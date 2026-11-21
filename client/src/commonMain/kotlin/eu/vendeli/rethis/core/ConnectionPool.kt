package eu.vendeli.rethis.core

import eu.vendeli.rethis.codecs.connection.PingCommandCodec
import eu.vendeli.rethis.configuration.ReThisConfiguration
import eu.vendeli.rethis.shared.types.CommandTimeoutException
import eu.vendeli.rethis.types.common.RConnection
import eu.vendeli.rethis.types.interfaces.AcquireFailure
import eu.vendeli.rethis.types.interfaces.DisposeReason
import eu.vendeli.rethis.types.interfaces.ExperimentalReThisMetricsApi
import eu.vendeli.rethis.utils.CLIENT_NAME
import eu.vendeli.rethis.utils.IO_OR_UNCONFINED
import io.ktor.network.sockets.*
import io.ktor.util.logging.*
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.decrementAndFetch
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.TimeSource

@OptIn(ExperimentalAtomicApi::class, ExperimentalReThisMetricsApi::class)
internal class ConnectionPool(
    private val address: SocketAddress,
    private val cfg: ReThisConfiguration,
    private val connectionFactory: ConnectionFactory,
    rootJob: Job,
) {
    init {
        require(cfg.pool.checkInterval > Duration.ZERO) {
            "pool.checkInterval must be positive, got ${cfg.pool.checkInterval}"
        }
    }

    private val name = "$CLIENT_NAME|${cfg::class.simpleName}Pool@${this::class.hashCode()}"
    private val logger = cfg.loggerFactory.get("eu.vendeli.rethis.ConnectionPool")
    private val addressLabel: String = address.toString()
    private val poolJob = Job(rootJob)
    private val scope = CoroutineScope(
        Dispatchers.IO_OR_UNCONFINED + CoroutineName(name) + poolJob,
    )
    private val idleConnectionsCount = AtomicInt(0)
    private val borrowCount = AtomicInt(0)

    private val idleCapacity = cfg.pool.maxIdleConnections.coerceAtMost(cfg.maxConnections)
    private val idleConnections = Channel<RConnection>(idleCapacity) { connectionFactory.dispose(it) }
    private val pending = Channel<CompletableDeferred<RConnection>>(cfg.pool.maxPendingConnections) {
        it.completeExceptionally(IllegalStateException("Pool is closed"))
    }

    init {
        logger.info("Initializing connection pool for $address")
        populatePool()
        runObserver()
    }

    private fun populatePool() = scope.launch(Dispatchers.IO_OR_UNCONFINED + Job(poolJob)) {
        val populationJob = currentCoroutineContext()[Job]!!
        repeat(cfg.pool.minIdleConnections) {
            launch(Job(populationJob)) { prepopulateOne() }
        }
        populationJob.invokeOnCompletion { logger.info("Connection pool initialized") }
    }

    // A failed attempt (refused connection, handshake error) must not escape into the uncaught-exception handler.
    private suspend fun prepopulateOne() {
        val rec = cfg.metricsRecorder
        val conn = try {
            connectionFactory.createConnOrNull(address)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.warn("Failed to prepopulate a connection to $address", e)
            null
        }
        if (conn == null) {
            rec?.onConnectionCreated(addressLabel, success = false)
            return
        }
        rec?.onConnectionCreated(addressLabel, success = true)
        // an acquire() may already wait for a connection because the factory limit was reached by this very one
        if (pending.handOff(conn)) return
        if (!offerIdle(conn)) {
            connectionFactory.dispose(conn)
            rec?.onConnectionDisposed(addressLabel, DisposeReason.CLOSE)
        }
    }

    internal val idleCount: Int get() = idleConnectionsCount.load()

    fun haveIdleConnections() = idleConnectionsCount.load() > 0

    // ThrowsCount: each throw routes a distinct failure mode (timeout vs. other) into
    // the correct AcquireFailure observation reason; consolidating into a single catch
    // with `is TimeoutCancellationException` triggers the InstanceOfCheckForException rule.
    @Suppress("ThrowsCount")
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun acquire(): RConnection {
        val rec = cfg.metricsRecorder
        val obs = rec?.connectionAcquireStarted(addressLabel)
        try {
            return withAcquireTimeout(cfg.connectionAcquireTimeout) aq@{
                borrowCount.incrementAndFetch()
                // fastest path
                val idleConn = idleConnections
                    .tryReceive()
                    .getOrNull()
                    ?.also { idleConnectionsCount.decrementAndFetch() }
                    ?.healthyOrNull()
                if (idleConn != null) {
                    obs?.acquired(fastPath = true)
                    return@aq idleConn
                }

                // medium path
                val conn = connectionFactory.createConnOrNull(address)
                if (conn != null) {
                    rec?.onConnectionCreated(addressLabel, success = true)
                    obs?.acquired(fastPath = false)
                    return@aq conn
                }
                rec?.onConnectionCreated(addressLabel, success = false)

                // slow path
                val deferred = CompletableDeferred<RConnection>()
                pending.send(deferred)
                try {
                    val acquired = deferred.await()
                    obs?.acquired(fastPath = false)
                    return@aq acquired
                } catch (e: Throwable) {
                    // Cancellation/timeout race: a concurrent release() may have completed
                    // `deferred` between our suspension cancellation and this catch. Cancel
                    // the deferred (no-op if already completed) so any later release that
                    // pulls it from `pending` falls through to fillPool, and dispose any
                    // race-delivered connection so it doesn't leak.
                    deferred.cancel()
                    if (deferred.isCompleted && !deferred.isCancelled) {
                        runCatching { deferred.getCompleted() }
                            .getOrNull()
                            ?.let { connectionFactory.dispose(it) }
                    }
                    throw e
                }
            }
        } catch (e: TimeoutCancellationException) {
            obs?.failed(AcquireFailure.TIMEOUT)
            throw e
        } catch (e: CommandTimeoutException) {
            obs?.failed(AcquireFailure.TIMEOUT)
            throw e
        } catch (e: Throwable) {
            obs?.failed(AcquireFailure.OTHER)
            throw e
        }
    }

    @OptIn(InternalAPI::class)
    fun release(conn: RConnection) {
        val rec = cfg.metricsRecorder
        if (!conn.input.readBuffer.exhausted()) {
            connectionFactory.dispose(conn)
            rec?.onConnectionDisposed(addressLabel, DisposeReason.LEFTOVER_BYTES)
            rec?.onConnectionReleased(addressLabel)
            return
        }
        conn.lastTouchedAt = TimeSource.Monotonic.markNow()
        rec?.onConnectionReleased(addressLabel)
        // Drain any cancelled awaiters whose `complete(conn)` would silently drop
        // the connection; hand off to the first live awaiter or fall through.
        while (true) {
            val def = pending.tryReceive().getOrNull() ?: break
            if (def.complete(conn)) return
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
            while (!pending.isEmpty) delay(100.milliseconds)
        }
        close()
    }

    fun close() {
        pending.cancel()
        idleConnections.cancel()
        scope.cancel()

        val rec = cfg.metricsRecorder
        while (true) {
            val element = idleConnections.tryReceive().getOrNull() ?: break
            connectionFactory.dispose(element)
            rec?.onConnectionDisposed(addressLabel, DisposeReason.CLOSE)
        }
    }

    private fun runObserver() = scope.launch(SupervisorJob(poolJob)) {
        logger.debug { "Starting connection pool observer" }
        while (isActive) {
            delay(cfg.pool.checkInterval)
            adjustPoolSize()
            cfg.metricsRecorder?.onPoolSample(addressLabel, idleConnectionsCount.load())
        }
    }

    private suspend fun adjustPoolSize() {
        val burstRate = burstRate(borrowCount.load(), cfg.pool.checkInterval)
        val idleNow = idleConnectionsCount.load()
        val expandBy = expandSize(burstRate, idleNow, idleCapacity)

        if (expandBy > 0) expandPool(expandBy)
        else if (idleNow > cfg.pool.minIdleConnections && burstRate - idleNow <= 0.0) shrinkPool()

        borrowCount.store(0)
    }

    private suspend fun expandPool(by: Int) {
        val rec = cfg.metricsRecorder
        if (connectionFactory.isReachedLimit()) {
            logger.debug { "Pool expanding attempt failed. Max connections reached." }
            return
        }
        repeat(by) {
            val conn = connectionFactory.createConnOrNull(address)
            if (conn == null) {
                rec?.onConnectionCreated(addressLabel, success = false)
                return
            }
            rec?.onConnectionCreated(addressLabel, success = true)
            // Hand off to the first live awaiter; drain cancelled deferreds left
            // by acquire() timeouts so they cannot swallow the fresh connection.
            var delivered = false
            while (true) {
                val def = pending.tryReceive().getOrNull() ?: break
                if (def.complete(conn)) {
                    delivered = true
                    break
                }
            }
            if (!delivered) conn.fillPool()
        }
    }

    private fun shrinkPool() {
        val rec = cfg.metricsRecorder
        val idleNow = idleConnectionsCount.load()
        val excess = idleNow - cfg.pool.minIdleConnections
        if (excess <= 0) return

        val toDrop = min((excess * cfg.pool.shrinkRatio).roundToInt(), cfg.pool.maxShrinkSize)
        repeat(toDrop) {
            idleConnections.tryReceive().getOrNull()?.let { conn ->
                connectionFactory.dispose(conn)
                idleConnectionsCount.decrementAndFetch()
                rec?.onConnectionDisposed(addressLabel, DisposeReason.SHRINK)
            }
        }
    }

    private fun RConnection.fillPool() {
        if (!offerIdle(this)) connectionFactory.dispose(this)
    }

    // The counter is raised before the send: once the connection is in the channel a concurrent
    // acquire() may take it and decrement at once, which would otherwise drive the counter negative.
    private fun offerIdle(conn: RConnection): Boolean {
        idleConnectionsCount.incrementAndFetch()
        if (idleConnections.trySend(conn).isSuccess) return true
        idleConnectionsCount.decrementAndFetch()
        return false
    }

    private suspend inline fun RConnection.healthyOrNull(): RConnection? {
        if (!cfg.pool.connectionHealthCheck) return this
        // Skip the PING when the connection is "freshly used"; an active connection
        // does not silently die between two adjacent borrows.
        if (lastTouchedAt.elapsedNow() < cfg.pool.connectionHealthCheckInterval) return this

        // Bound the health-check PING independently of commandTimeout / socket.timeout
        // so a half-dead connection cannot stall the next borrower.
        runCatching {
            withTimeout(cfg.pool.connectionHealthCheckTimeout) {
                doRequest(PingCommandCodec.encode(charset = cfg.charset, message = null).data)
            }
        }.onFailure {
            connectionFactory.dispose(this)
            cfg.metricsRecorder?.onConnectionDisposed(addressLabel, DisposeReason.HEALTH_FAIL)
            return null
        }

        lastTouchedAt = TimeSource.Monotonic.markNow()
        return this
    }

    internal companion object {
        /** Borrows per second over [interval], which may be shorter or fractionally longer than a second. */
        fun burstRate(borrows: Int, interval: Duration): Double = borrows / interval.toDouble(DurationUnit.SECONDS)

        /** Number of connections to add: the pressure over the idle pool, never past the idle capacity. */
        fun expandSize(burstRate: Double, idleNow: Int, idleCapacity: Int): Int {
            val pressure = burstRate - idleNow
            if (pressure.isNaN() || pressure <= 0.0) return 0
            val room = (idleCapacity - idleNow).coerceAtLeast(0)
            return ceil(pressure).coerceAtMost(room.toDouble()).toInt()
        }
    }
}

// Hands [conn] to the first live awaiter, draining cancelled deferreds left by acquire() timeouts.
private fun Channel<CompletableDeferred<RConnection>>.handOff(conn: RConnection): Boolean {
    while (true) {
        val def = tryReceive().getOrNull() ?: return false
        if (def.complete(conn)) return true
    }
}
