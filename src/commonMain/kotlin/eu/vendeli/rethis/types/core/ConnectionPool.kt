package eu.vendeli.rethis.types.core

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.utils.readRedisMessage
import eu.vendeli.rethis.utils.sendRequest
import eu.vendeli.rethis.utils.writeRedisValue
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.network.tls.*
import io.ktor.util.logging.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
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

    private val job = SupervisorJob(client.rootJob)
    private val poolScope = CoroutineScope(
        client.cfg.poolConfiguration.dispatcher + job + CoroutineName("ReThis-ConnectionPool"),
    )
    private val connections = Channel<Connection>(client.cfg.poolConfiguration.poolSize)
    private val selector = SelectorManager(poolScope.coroutineContext)

    internal suspend fun createConn(): Connection {
        logger.trace("Creating connection to ${client.address}")
        val conn = aSocket(selector)
            .tcp()
            .connect(client.address.socket)
            .let { socket ->
                client.cfg.tlsConfig?.let {
                    socket.tls(selector.coroutineContext, it)
                } ?: socket
            }.connection()

        val reqBuffer = Buffer()
        var requests = 0

        if (client.cfg.auth != null) client.cfg.auth?.run {
            logger.trace("Authenticating to ${client.address}.")
            reqBuffer.writeRedisValue(listOfNotNull("AUTH".toArg(), username?.toArg(), password.toArg()))
            requests++
        }

        client.cfg.db?.takeIf { it > 0 }?.let {
            logger.trace("Selecting database $it to ${client.address}.")
            reqBuffer.writeRedisValue(listOf("SELECT".toArg(), it.toArg()))
            requests++
        }

        reqBuffer.writeRedisValue(listOf("HELLO".toArg(), client.protocol.literal.toArg()))
        requests++

        logger.trace("Sending connection establishment requests ($requests)")
        conn.sendRequest(reqBuffer)
        repeat(requests) {
            logger.trace("Connection establishment response: " + conn.input.readRedisMessage(client.cfg.charset))
        }

        return conn
    }

    fun prepare() = client.rethisCoScope.launch {
        logger.info("Filling ConnectionPool with connections")
        repeat(client.cfg.poolConfiguration.poolSize) {
            client.rethisCoScope.launch { connections.trySend(createConn()) }
        }
    }

    suspend fun acquire(): Connection = connections.receive()

    fun release(connection: Connection) {
        handle(connection)
    }

    private fun handle(connection: Connection) = poolScope.launch {
        logger.trace("Releasing connection ${connection.socket}")
        val cfg = client.cfg.reconnectionStrategy
        if (cfg.doHealthCheck && connection.input.isClosedForRead) { // connection is corrupted
            logger.warn("Connection ${connection.socket} is corrupted, refilling")
            connection.socket.close()
            refill()
        } else {
            connections.send(connection)
        }
    }

    private tailrec suspend fun refill(attempt: Int = 1) {
        val cfg = client.cfg.reconnectionStrategy
        var ex: Throwable? = null
        if (cfg.reconnectAttempts >= attempt) {
            if (ex == null) logger.warn("Connection refills failed, maximum attempts reached")
            else logger.warn("Connection refills failed, maximum attempts reached", ex)
            return
        }
        delay(attempt * cfg.reconnectDelay)
        logger.trace("Refilling ConnectionPool. Attempt $attempt")
        runCatching { createConn() }.onSuccess {
            connections.send(it)
            return
        }.onFailure {
            if (ex != null) ex.addSuppressed(it) else ex = it
        }
        logger.debug("Connection refill failed, remaining attempts: ${cfg.reconnectAttempts - attempt}")
        refill(attempt + 1)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun disconnect() = runBlocking {
        logger.debug("Disconnecting from Redis")
        while (!connections.isEmpty) {
            connections.receive().socket.close()
        }
    }
}

@OptIn(ExperimentalContracts::class)
internal suspend inline fun <R> ConnectionPool.use(block: (Connection) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    var exception: Throwable? = null
    val connection = acquire()
    logger.trace("Using ${connection.socket} for request")
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
    }
}
