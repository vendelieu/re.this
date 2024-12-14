package eu.vendeli.rethis.types.core

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.utils.coLaunch
import eu.vendeli.rethis.utils.readRedisMessage
import eu.vendeli.rethis.utils.writeRedisValue
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.network.tls.*
import io.ktor.util.logging.*
import io.ktor.utils.io.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.io.Buffer
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal class ConnectionPool(
    internal val client: ReThis,
    private val address: SocketAddress,
) {
    internal val logger = KtorSimpleLogger("eu.vendeli.rethis.ConnectionPool")

    @OptIn(ExperimentalCoroutinesApi::class)
    internal val isEmpty: Boolean get() = connections.isEmpty

    private val connections = Channel<Connection>(client.cfg.maxConnections)
    private val selector = SelectorManager(client.cfg.dispatcher + client.rootJob)

    internal suspend fun createConn(): Connection {
        logger.trace("Creating connection to $address")
        val conn = aSocket(selector)
            .tcp()
            .connect(address)
            .let { socket ->
                client.cfg.tlsConfig?.let {
                    socket.tls(selector.coroutineContext, it)
                } ?: socket
            }.connection()

        val reqBuffer = Buffer()
        var requests = 0

        if (client.cfg.auth != null) client.cfg.auth?.run {
            logger.debug("Authenticating to $address with $this")
            reqBuffer.writeRedisValue(listOfNotNull("AUTH".toArg(), username?.toArg(), password.toArg()))
            requests++
        }

        client.cfg.db?.takeIf { it > 0 }?.let {
            requests++
            reqBuffer.writeRedisValue(listOf("SELECT".toArg(), it.toArg()))
        }

        reqBuffer.writeRedisValue(listOf("HELLO".toArg(), client.protocol.literal.toArg()))
        requests++

        conn.output.writeBuffer(reqBuffer)
        conn.output.flush()
        repeat(requests) {
            logger.trace("Connection establishment response: " + conn.input.readRedisMessage(client.cfg.charset))
        }

        return conn
    }

    fun prepare() = runBlocking {
        logger.info("Filling ConnectionPool with connections")
        repeat(client.cfg.maxConnections) {
            client.coLaunch { connections.trySend(createConn()) }
        }
    }

    suspend fun acquire(): Connection = connections.receive()

    suspend fun release(connection: Connection) = connections.send(connection)

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
