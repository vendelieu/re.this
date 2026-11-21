package eu.vendeli.rethis.core

import eu.vendeli.rethis.codecs.connection.AuthCommandCodec
import eu.vendeli.rethis.codecs.connection.ClientSetNameCommandCodec
import eu.vendeli.rethis.codecs.connection.HelloCommandCodec
import eu.vendeli.rethis.codecs.cluster.ReadOnlyCommandCodec
import eu.vendeli.rethis.codecs.connection.SelectCommandCodec
import eu.vendeli.rethis.configuration.ClusterConfiguration
import eu.vendeli.rethis.configuration.ReThisConfiguration
import eu.vendeli.rethis.shared.decoders.general.RTypeDecoder
import eu.vendeli.rethis.shared.request.connection.HelloAuth
import eu.vendeli.rethis.shared.types.ReThisException
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.types.common.RConnection
import eu.vendeli.rethis.types.common.ReadFrom
import eu.vendeli.rethis.types.common.RespVer
import eu.vendeli.rethis.types.common.rConnection
import eu.vendeli.rethis.utils.CLIENT_NAME
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.network.tls.*
import io.ktor.util.logging.*
import io.ktor.utils.io.charsets.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Semaphore
import kotlinx.io.Buffer
import kotlin.concurrent.Volatile

internal class ConnectionFactory(
    private val cfg: ReThisConfiguration,
    rootJob: Job,
) {
    init {
        require((cfg.db ?: 0) >= 0) { "db must not be negative, got ${cfg.db}" }
    }

    private val logger = cfg.loggerFactory.get("eu.vendeli.rethis.core.ConnectionFactory")
    private val connections = Semaphore(cfg.maxConnections)
    private val scope = CoroutineScope(
        cfg.generalDispatcher + CoroutineName("$CLIENT_NAME|ConnectionFactory") + Job(rootJob),
    )
    private val selector = SelectorManager(scope.coroutineContext)

    @Volatile
    private var helloUnsupported = false

    fun isReachedLimit() = connections.availablePermits == 0

    suspend fun createConnOrNull(address: SocketAddress): RConnection? {
        if (!connections.tryAcquire()) {
            logger.debug { "Creating connection attempt failed. Max connections reached." }
            return null
        }

        var socket: Socket? = null
        try {
            socket = aSocket(selector)
                .tcp()
                .connect(address) {
                    cfg.socket.run {
                        timeout?.let { socketTimeout = it }
                        linger?.let { lingerSeconds = it }
                        this@connect.keepAlive = keepAlive
                        this@connect.noDelay = noDelay
                    }
                }.let { s ->
                    cfg.tls?.let { s.tls(selector.coroutineContext, it.withServerName(address)) } ?: s
                }
            val conn = socket.rConnection()
            prepareConnection(conn)
            return conn
        } catch (e: Throwable) {
            // Cancellation (e.g. enclosing withTimeout) used to drop the half-built
            // socket on the floor and only release the permit on GC, eventually
            // starving the pool / hitting Redis maxclients.
            socket?.runCatching { dispose() }
            connections.release()
            throw e
        }
    }

    suspend fun prepareConnection(conn: RConnection) {
        val cfgAuth = cfg.auth
        val cfgDb = cfg.db
        // Redis older than 6 has no HELLO; once the server said so, RESP2 setup uses AUTH / CLIENT SETNAME instead.
        val useHello = cfg.protocol == RespVer.V3 || !helloUnsupported
        val needsHello = useHello && (cfg.protocol == RespVer.V3 || cfgAuth != null || cfg.pool.setClientName)
        val needsLegacySetup = !useHello && (cfgAuth != null || cfg.pool.setClientName)
        val needsSelect = cfgDb != null && cfgDb > 0
        val sendsHello = needsHello || (useHello && needsSelect)
        // a replica only serves reads on a connection that has sent READONLY (it applies per connection and is
        // ignored by masters), so every cluster connection is switched when reads may be served by replicas
        val needsReadOnly = cfg is ClusterConfiguration && cfg.readFromStrategy !== ReadFrom.Master
        // HELLO carries both auth and the client name, so a RESP2 connection that only needs a name still goes through it.
        if (!needsHello && !needsLegacySetup && !needsSelect && !needsReadOnly) return

        val request = buildList {
            if (sendsHello) add(helloRequest())
            if (needsLegacySetup) addAll(legacySetupRequests())
            if (needsSelect) add(SelectCommandCodec.encode(cfg.charset, cfgDb!!.toLong()).data)
            if (needsReadOnly) add(ReadOnlyCommandCodec.encode(cfg.charset).data)
        }
        val responseBuffer = conn.doBatchRequest(request)
        val responses = List(request.size) { RTypeDecoder.decode(responseBuffer, cfg.charset) }
        val helloError = (responses.firstOrNull() as? RType.Error)?.takeIf { sendsHello && isUnknownCommand(it) }
        if (helloError != null) {
            onHelloUnsupported(helloError)
            return prepareConnection(conn)
        }
        responses.forEach { if (it is RType.Error) throw it.exception }
    }

    private fun helloRequest(): Buffer = HelloCommandCodec
        .encode(
            Charsets.UTF_8,
            cfg.protocol.literal.toLong(),
            cfg.auth?.let { HelloAuth(it.username ?: "default", it.password) },
            CLIENT_NAME.takeIf { cfg.pool.setClientName },
        ).data

    private fun onHelloUnsupported(error: RType.Error) {
        if (cfg.protocol == RespVer.V3) {
            throw ReThisException(
                "RESP3 requires Redis 6 or newer (HELLO is not supported): ${error.exception.message}",
            )
        }
        helloUnsupported = true
    }

    // pre-6 AUTH knows only a password, so the "default" user is sent as the bare password form
    private fun legacySetupRequests(): List<Buffer> = buildList {
        cfg.auth?.let { auth ->
            add(AuthCommandCodec.encode(cfg.charset, auth.username?.takeUnless { it == "default" }, auth.password).data)
        }
        if (cfg.pool.setClientName) add(ClientSetNameCommandCodec.encode(cfg.charset, CLIENT_NAME).data)
    }

    private fun isUnknownCommand(error: RType.Error) =
        error.exception.message?.contains("unknown command", ignoreCase = true) == true

    fun dispose(conn: RConnection) {
        connections.runCatching { release() }.onFailure {
            logger.debug("Failed to release connection", it)
        }
        conn.socket.dispose()
    }
}

/**
 * Config handed to the TLS handshake for a connection to [address]. Ktor verifies the certificate host and
 * sends SNI only when the config names a server, so an unset name defaults to the host being connected to;
 * an explicit one always wins.
 */
internal fun TLSConfig.withServerName(address: SocketAddress): TLSConfig {
    val host = (address as? InetSocketAddress)?.hostname ?: return this
    return withDefaultServerName(host)
}

/** [this] when it already names a server (or the target has no TLS support), otherwise a copy naming [host]. */
internal expect fun TLSConfig.withDefaultServerName(host: String): TLSConfig
