package eu.vendeli.rethis.topology

import eu.vendeli.rethis.shared.utils.CRC16
import eu.vendeli.rethis.types.common.Address
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.nio.channels.ClosedChannelException
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

/** A slot block of a `CLUSTER SLOTS` reply: [range] served by [master] with optional [replicas]. */
class SlotBlock(
    val range: IntRange,
    val master: Address,
    val replicas: List<Address> = emptyList(),
)

private val Address.inet get() = socket as InetSocketAddress

/** `host:port` as written in redirect errors. */
fun Address.hostPort() = "${inet.hostname}:${inet.port}"

/** RESP2 `CLUSTER SLOTS` reply for [blocks]. */
fun clusterSlotsReply(vararg blocks: SlotBlock): String = FakeRespServer.array(
    *blocks
        .map { block ->
            val nodes = (listOf(block.master) + block.replicas).map {
                FakeRespServer.array(
                    FakeRespServer.bulk(it.inet.hostname),
                    ":${it.inet.port}\r\n",
                    FakeRespServer.bulk("node-${it.inet.port}"),
                )
            }
            FakeRespServer.array(":${block.range.first}\r\n", ":${block.range.last}\r\n", *nodes.toTypedArray())
        }.toTypedArray(),
)

/**
 * Cluster node that answers with replies that decode for the common commands (`GET` replies `v:<key>`,
 * `MGET` replies one value per key, `DEL` the key count) and lets a test script individual replies through
 * [override]. Every received command is recorded.
 */
class ScriptedClusterNode(
    @Volatile var slotsReply: String = "*0\r\n",
    @Volatile var override: (List<String>) -> String? = { null },
    /** When set the node is a replica of this master: reads on a connection that has not sent READONLY get MOVED. */
    @Volatile var replicaOf: Address? = null,
    /** Milliseconds to wait before answering a command, to simulate a slow or blocking node. */
    @Volatile var delayFor: (List<String>) -> Long = { 0 },
) : AutoCloseable {
    private val selector = SelectorManager(Dispatchers.IO)
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val server = runBlocking { aSocket(selector).tcp().bind("127.0.0.1", 0) }
    private val received = CopyOnWriteArrayList<List<String>>()
    private val accepted = AtomicInteger()

    @Volatile private var closing = false

    val address: Address = (server.localAddress as InetSocketAddress).let { Address(it.hostname, it.port) }
    val commands: List<List<String>> get() = received.toList()
    val acceptedConnections: Int get() = accepted.get()

    init {
        scope.launch { acceptLoop() }
    }

    // Accepts until the server socket is closed; a failure that only arises from shutdown ends the loop quietly.
    private suspend fun acceptLoop() {
        try {
            while (true) {
                val socket = server.accept()
                accepted.incrementAndGet()
                scope.launch { serve(socket) }
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            if (!closing) throw e
        }
    }

    fun commandCount(vararg prefix: String) = received.count { cmd ->
        cmd.size >= prefix.size && prefix.indices.all { cmd[it].equals(prefix[it], ignoreCase = true) }
    }

    private suspend fun serve(socket: Socket) {
        val input = socket.openReadChannel()
        val output = socket.openWriteChannel(autoFlush = true)
        var readOnly = false
        try {
            while (true) {
                val command = readCommand(input) ?: break
                received += command
                if (command.first().equals("READONLY", ignoreCase = true)) readOnly = true
                val master = replicaOf
                val reply = if (master != null && !readOnly && command.first().uppercase() in READS) {
                    "-MOVED ${CRC16.slot(command[1].toByteArray())} ${master.inet.hostname}:${master.inet.port}\r\n"
                } else {
                    override(command) ?: defaultReply(command)
                }
                delayFor(command).takeIf { it > 0 }?.let { kotlinx.coroutines.delay(it) }
                output.writeStringUtf8(reply)
            }
        } catch (_: Exception) {
            // peer went away
        } finally {
            socket.close()
        }
    }

    private fun defaultReply(command: List<String>): String = when (command.first().uppercase()) {
        "CLUSTER" -> slotsReply
        "PING" -> "+PONG\r\n"
        "GET" -> FakeRespServer.bulk("v:${command[1]}")
        "MGET" -> FakeRespServer.array(*command.drop(1).map { FakeRespServer.bulk("v:$it") }.toTypedArray())
        "DEL" -> ":${command.size - 1}\r\n"
        else -> "+OK\r\n"
    }

    private suspend fun readCommand(input: ByteReadChannel): List<String>? {
        val header = input.readUTF8Line() ?: return null
        if (!header.startsWith("*")) return listOf(header)
        return List(header.drop(1).toInt()) {
            input.readUTF8Line()
            input.readUTF8Line() ?: return null
        }
    }

    override fun close() {
        closing = true
        server.close()
        scope.cancel()
        runBlocking { scope.coroutineContext[Job]?.join() }
        selector.close()
    }
}

private val READS = setOf("GET", "MGET")
