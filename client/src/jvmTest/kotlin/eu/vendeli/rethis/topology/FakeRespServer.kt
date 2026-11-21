package eu.vendeli.rethis.topology

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
import java.util.concurrent.ConcurrentHashMap
import java.nio.channels.ClosedChannelException
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

/**
 * Minimal RESP2 server for topology tests. Answers `PING`, `SENTINEL GET-MASTER-ADDR-BY-NAME` and
 * `SENTINEL REPLICAS` and `CLUSTER SLOTS` from the configured raw replies (`HELLO` too), acknowledges `SUBSCRIBE` and keeps the connection open so
 * [publish] can push messages, and replies `+OK` to anything else. Every received command is recorded.
 */
class FakeRespServer(
    @Volatile var masterReply: String = "*0\r\n",
    @Volatile var replicasReply: String = "*0\r\n",
    @Volatile var clusterSlotsReply: String = "*0\r\n",
    @Volatile var helloReply: String = "+OK\r\n",
    @Volatile var silent: Boolean = false,
    /** Raw reply per upper-case command name; takes precedence over the built-in answers. */
    @Volatile var replies: Map<String, String> = emptyMap(),
    /** Upper-case command names that make the server close the socket instead of answering. */
    @Volatile var dropOn: Set<String> = emptySet(),
    /** Port to bind; `0` picks a free one. */
    port: Int = 0,
) : AutoCloseable {
    private val selector = SelectorManager(Dispatchers.IO)
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val server = runBlocking { aSocket(selector).tcp().bind("127.0.0.1", port) }
    private val subscribers = ConcurrentHashMap<String, MutableList<ByteWriteChannel>>()
    private val received = CopyOnWriteArrayList<List<String>>()
    private val accepted = AtomicInteger()

    @Volatile private var closing = false

    /** Raw replies by upper-case command name, taking precedence over the built-in ones. */
    val commandReplies = ConcurrentHashMap<String, String>()

    /** Milliseconds to wait before answering the upper-case command name, simulating a slow or blocking server. */
    val replyDelays = ConcurrentHashMap<String, Long>()

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

    suspend fun publish(channel: String, message: String) {
        val frame = "*3\r\n\$7\r\nmessage\r\n${bulk(channel)}${bulk(message)}"
        subscribers[channel].orEmpty().forEach { it.writeStringUtf8(frame) }
    }

    fun subscribedChannels(): Set<String> = subscribers.keys

    private suspend fun serve(socket: Socket) {
        val input = socket.openReadChannel()
        val output = socket.openWriteChannel(autoFlush = true)
        try {
            while (true) {
                val command = readCommand(input) ?: break
                received += command
                if (command.firstOrNull()?.uppercase() in dropOn) break
                if (!silent) reply(command, output)
            }
        } catch (_: Exception) {
            // peer went away
        } finally {
            socket.close()
        }
    }

    private suspend fun reply(command: List<String>, output: ByteWriteChannel) {
        val name = command.firstOrNull()?.uppercase()
        val sub = command.getOrNull(1)?.uppercase()
        name?.let { replyDelays[it] }?.let { kotlinx.coroutines.delay(it) }
        when {
            name != null && name in replies -> output.writeStringUtf8(replies.getValue(name))

            name != null && commandReplies.containsKey(name) -> output.writeStringUtf8(commandReplies.getValue(name))

            name == "HELLO" -> output.writeStringUtf8(helloReply)

            name == "PING" -> output.writeStringUtf8("+PONG\r\n")

            name == "SENTINEL" && sub == "GET-MASTER-ADDR-BY-NAME" -> output.writeStringUtf8(masterReply)

            name == "SENTINEL" && (sub == "REPLICAS" || sub == "SLAVES") -> output.writeStringUtf8(replicasReply)

            name == "CLUSTER" && sub == "SLOTS" -> output.writeStringUtf8(clusterSlotsReply)

            name == "SUBSCRIBE" -> command.drop(1).forEachIndexed { i, channel ->
                subscribers.getOrPut(channel) { CopyOnWriteArrayList() }.add(output)
                output.writeStringUtf8("*3\r\n\$9\r\nsubscribe\r\n${bulk(channel)}:${i + 1}\r\n")
            }

            else -> output.writeStringUtf8("+OK\r\n")
        }
    }

    private suspend fun readCommand(input: ByteReadChannel): List<String>? {
        val header = input.readUTF8Line() ?: return null
        if (!header.startsWith("*")) return listOf(header)
        return List(header.drop(1).toInt()) {
            input.readUTF8Line() // bulk length header
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

    companion object {
        fun bulk(value: String) = "\$${value.toByteArray().size}\r\n$value\r\n"

        fun array(vararg items: String) = "*${items.size}\r\n${items.joinToString("")}"

        /** One `SENTINEL REPLICAS` entry in the RESP2 wire shape: a flat field/value array. */
        fun replicaEntry(host: String, port: Int, flags: String) = array(
            *listOf(
                "name" to "$host:$port",
                "ip" to host,
                "port" to port.toString(),
                "runid" to "43d1e09ef4325bac4c25c1108e2b8f8cc78d1d65",
                "flags" to flags,
                "link-pending-commands" to "0",
                "down-after-milliseconds" to "1000",
                "role-reported" to "slave",
                "master-link-status" to "ok",
                "slave-priority" to "100",
                "replica-announced" to "1",
            ).flatMap { (k, v) -> listOf(bulk(k), bulk(v)) }.toTypedArray(),
        )

        fun masterAddrReply(address: Address): String {
            val socket = address.socket as InetSocketAddress
            return array(bulk(socket.hostname), bulk(socket.port.toString()))
        }
    }
}
