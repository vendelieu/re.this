package eu.vendeli.rethis.core

import eu.vendeli.rethis.codecs.connection.PingCommandCodec
import eu.vendeli.rethis.configuration.ReThisConfiguration
import eu.vendeli.rethis.types.common.RConnection
import eu.vendeli.rethis.utils.CLIENT_NAME
import eu.vendeli.rethis.utils.IO_OR_UNCONFINED
import io.ktor.network.sockets.*
import io.ktor.util.logging.debug
import io.ktor.utils.io.charsets.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.decrementAndFetch
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt

// todo add metrics
@OptIn(ExperimentalAtomicApi::class)
internal class ConnectionPool(
    private val address: SocketAddress,
    private val cfg: ReThisConfiguration,
    private val connectionFactory: ConnectionFactory,
    rootJob: Job,
) {
    private val name = "$CLIENT_NAME|${cfg::class.simpleName}Pool@${this::class.hashCode()}"
    private val logger = cfg.loggerFactory.get("eu.vendeli.rethis.ConnectionPool")
    private val scope = CoroutineScope(
        Dispatchers.IO_OR_UNCONFINED + CoroutineName(name) + Job(rootJob),
    )
    private val idleConnectionsCount = AtomicInt(0)
    private val borrowCount = AtomicInt(0)

    private val idleConnections = Channel<RConnection>(
        cfg.pool.maxIdleConnections.coerceAtMost(cfg.maxConnections),
    ) { connectionFactory.dispose(it) }
    private val pending = Channel<CompletableDeferred<RConnection>>(cfg.pool.maxPendingConnections) {
        it.completeExceptionally(IllegalStateException("Pool is closed"))
    }

    init {
        logger.info("Initializing connection pool for $address")
        logger.debug { "Pool configuration: ${cfg.pool}" }
        populatePool()
        runObserver()
    }

    private fun populatePool() = scope.launch(Dispatchers.IO_OR_UNCONFINED + Job()) {
        val populationJob = currentCoroutineContext()[Job]!!
        repeat(cfg.pool.minIdleConnections) {
            launch(populationJob) {
                val conn = connectionFactory.createConnOrNull(address) ?: return@launch
                idleConnections.trySend(conn).onSuccess {
                    idleConnectionsCount.incrementAndFetch()
                }.onFailure {
                    connectionFactory.dispose(conn)
                }
            }
        }
        populationJob.invokeOnCompletion { logger.info("Connection pool initialized") }
    }

    fun haveIdleConnections() = idleConnectionsCount.load() < cfg.pool.maxIdleConnections

    suspend fun acquire(): RConnection = withTimeout(cfg.connectionAcquireTimeout) aq@{
        borrowCount.incrementAndFetch()
        // fastest path
        val idleConn = idleConnections.tryReceive().getOrNull()?.healthyOrNull()
        if (idleConn != null) {
            return@aq idleConn
        }

        // medium path
        val conn = connectionFactory.createConnOrNull(address)
        if (conn != null) {
            return@aq conn
        }

        // slow path
        val deferred = CompletableDeferred<RConnection>()
        pending.send(deferred)
        return@aq deferred.await()
    }

    fun release(conn: RConnection) {
        pending.tryReceive().onSuccess {
            it.complete(conn)
            return
        }

        conn.fillPool()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun closeGracefully() {
        pending.close()
        idleConnections.close()
        // Wait 30 seconds for current operations
        withTimeoutOrNull(cfg.pool.gracefulClosePeriod) {
            // it's ok to use isEmpty here because we're not processing pending connections but waiting to close
            while (!pending.isEmpty) delay(100)
        }
        close()
    }

    fun close() {
        pending.cancel()
        idleConnections.cancel()
        scope.cancel()

        while (true) {
            val element = idleConnections.tryReceive().getOrNull() ?: break
            connectionFactory.dispose(element)
        }
    }

    private fun runObserver() = scope.launch(SupervisorJob()) {
        logger.debug { "Starting connection pool observer" }
        while (isActive) {
            delay(cfg.pool.checkInterval)
            adjustPoolSize()
        }
    }

    // borrows per second
    private fun getBurstRate(): Float =
        borrowCount.load().toFloat() / (cfg.pool.checkInterval.inWholeSeconds.toFloat())

    private suspend fun adjustPoolSize() {
        val burstRate = getBurstRate()
        val idleNow = idleConnectionsCount.load()
        val pressure = burstRate - idleNow

        if (pressure > 0f) expandPool(ceil(pressure).toInt())
        else if (idleNow > cfg.pool.minIdleConnections) shrinkPool()

        borrowCount.store(0)
    }

    private suspend fun expandPool(by: Int) {
        if (!connectionFactory.isReachedLimit()) repeat(by) {
            val conn = connectionFactory.createConnOrNull(address) ?: return
            pending.tryReceive().onSuccess {
                it.complete(conn)
                return
            }
            conn.fillPool()
        } else {
            logger.debug { "Pool expanding attempt failed. Max connections reached." }
        }
    }

    private fun shrinkPool() {
        val idleNow = idleConnectionsCount.load()
        val excess = idleNow - cfg.pool.minIdleConnections
        if (excess <= 0) return

        val toDrop = min((excess * cfg.pool.shrinkRatio).roundToInt(), cfg.pool.maxShrinkSize)
        repeat(toDrop) {
            idleConnections.tryReceive().getOrNull()?.let { conn ->
                connectionFactory.dispose(conn)
                idleConnectionsCount.decrementAndFetch()
            }
        }
    }

    private fun RConnection.fillPool() {
        idleConnections.trySend(this).onSuccess {
            idleConnectionsCount.incrementAndFetch()
        }.onFailure {
            connectionFactory.dispose(this)
        }
    }

    private suspend inline fun RConnection.healthyOrNull(): RConnection? {
        if (!cfg.pool.connectionHealthCheck) return this

        runCatching {
            doRequest(PingCommandCodec.encode(charset = Charsets.UTF_8, message = null).buffer)
        }.onFailure {
            connectionFactory.dispose(this)
            return null
        }

        return this
    }
}
