package eu.vendeli.rethis.types.core

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.utils.readResponseWrapped
import eu.vendeli.rethis.utils.sendRequest
import eu.vendeli.rethis.utils.writeRedisValue
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.network.tls.*
import io.ktor.util.logging.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import kotlinx.io.Buffer
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal class ConnectionPool(
    internal val client: ReThis,
) {
    internal val logger = KtorSimpleLogger("eu.vendeli.rethis.ConnectionPool")

    @OptIn(ExperimentalCoroutinesApi::class)
    internal val isEmpty: Boolean get() = connections.isEmpty

    private val poolJob = SupervisorJob(client.rootJob)
    private val poolScope = CoroutineScope(
        client.cfg.connectionConfiguration.dispatcher + poolJob + CoroutineName("ReThis|ConnectionPool"),
    )
    private val connections = Channel<RConnection>(client.cfg.connectionConfiguration.poolSize)
    private val coldPool = Channel<RConnection>(client.cfg.connectionConfiguration.poolSize)
    private val selector = SelectorManager(poolScope.coroutineContext)

    init {
        poolScope.launch { prepare() }
    }

    internal suspend fun createConn(): RConnection {
        logger.trace { "Creating connection to ${client.address}" }
        val conn = aSocket(selector)
            .tcp()
            .connect(client.address.socket) {
                client.cfg.socketConfiguration.run {
                    timeout?.let { socketTimeout = it }
                    linger?.let { lingerSeconds = it }
                    this@connect.keepAlive = keepAlive
                    this@connect.noDelay = noDelay
                }
            }.let { socket ->
                client.cfg.tlsConfig?.let {
                    socket.tls(selector.coroutineContext, it)
                } ?: socket
            }.rConnection()

        val reqBuffer = Buffer()
        var requests = 0

        if (client.cfg.auth != null) client.cfg.auth?.run {
            logger.trace { "Authenticating to ${client.address}." }
            reqBuffer.writeRedisValue(listOfNotNull("AUTH".toArg(), username?.toArg(), password.toArg()))
            requests++
        }

        client.cfg.db?.takeIf { it > 0 }?.let {
            logger.trace { "Selecting database $it to ${client.address}." }
            reqBuffer.writeRedisValue(listOf("SELECT".toArg(), it.toArg()))
            requests++
        }

        reqBuffer.writeRedisValue(listOf("HELLO".toArg(), client.protocol.literal.toArg()))
        requests++

        logger.trace { "Sending connection establishment requests ($requests)" }
        conn.sendRequest(reqBuffer)
        repeat(requests) {
            val response = conn.readResponseWrapped(client.cfg.charset)
            logger.trace { "Connection establishment response ($it): $response" }
        }

        return conn
    }

    @Suppress("OPT_IN_USAGE")
    fun prepare() = client.rethisCoScope.launch(Dispatchers.IO) {
        logger.info("Filling ConnectionPool with connections (${client.cfg.connectionConfiguration.poolSize})")
        cleaner()
        repeat(client.cfg.connectionConfiguration.poolSize) {
            launch { connections.trySend(createConn()) }
        }
    }

    suspend fun acquire(): RConnection = connections.receive()

    fun release(connection: RConnection) = poolScope.launch {
        coldPool.send(connection)
    }

    private val cleanerDispatcher = Dispatchers.Default.limitedParallelism(1)
    private val cleanerJob = SupervisorJob()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val cleanerScope = CoroutineScope(cleanerDispatcher + cleanerJob + CoroutineName("PoolCleaner"))

    @OptIn(DelicateCoroutinesApi::class)
    private fun cleaner() = GlobalScope.launch {
        cleanerScope.launch {
            while (isActive) {
                val connection = coldPool.tryReceive().getOrNull() ?: continue
                if (connection.input.isClosedForRead) {
                    logger.trace { "Cleaned connection ${connection.socket}" }
                    connection.socket.close()
                    connections.refill()
                    continue
                }
                connections.trySend(connection).onFailure { connection.socket.close() }
                coldPool.refill()
            }
        }
    }

    private fun Channel<RConnection>.refill() = cleanerScope.launch {
        val cfg = client.cfg.connectionConfiguration
        if (cfg.reconnectAttempts <= 0) return@launch
        var attempt = 0
        var ex: Throwable? = null

        while (attempt < cfg.reconnectAttempts) {
            attempt++
            logger.trace { "Refilling ConnectionPool. Attempt $attempt" }
            runCatching { createConn() }
                .onSuccess { conn ->
                    launch {
                        while (!trySend(conn).isSuccess) { yield() }
                    }
                    logger.trace { "Connection refilled with $conn" }
                    return@launch
                }.onFailure {
                    if (ex != null) ex.addSuppressed(it) else ex = it
                }

            logger.debug("Connection refill failed, remaining attempts: ${cfg.reconnectAttempts - attempt}")
            delay(attempt * cfg.reconnectDelay)
        }

        val logMsg = "Connection refills failed, maximum attempts reached"
        if (ex == null) logger.warn(logMsg)
        else logger.warn(logMsg, ex)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun disconnect() = runBlocking {
        logger.debug("Disconnecting from Redis")
        cleanerJob.cancelAndJoin()
        while (!connections.isEmpty) {
            connections.receive().socket.close()
        }
    }
}

@OptIn(ExperimentalContracts::class)
internal suspend inline fun <R> ConnectionPool.use(block: (RConnection) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    var exception: Throwable? = null
    val connection = acquire()
    logger.trace { "Using ${connection.socket} for request" }
    connection.status.lock()
    try {
        return block(connection)
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        when {
            exception?.cause == null -> release(connection)
            else -> try {
                release(connection)
            } catch (closeException: Throwable) {
                exception.cause?.addSuppressed(closeException)
                throw exception
            }
        }
        connection.status.unlock()
    }
}
