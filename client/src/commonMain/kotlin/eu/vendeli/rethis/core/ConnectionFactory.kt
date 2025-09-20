package eu.vendeli.rethis.core

import eu.vendeli.rethis.codecs.connection.HelloCommandCodec
import eu.vendeli.rethis.codecs.connection.SelectCommandCodec
import eu.vendeli.rethis.configuration.ReThisConfiguration
import eu.vendeli.rethis.shared.request.connection.HelloAuth
import eu.vendeli.rethis.types.common.RConnection
import eu.vendeli.rethis.types.common.RespVer
import eu.vendeli.rethis.types.common.rConnection
import eu.vendeli.rethis.utils.CLIENT_NAME
import eu.vendeli.rethis.utils.IO_OR_UNCONFINED
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.network.tls.*
import io.ktor.util.logging.*
import io.ktor.utils.io.charsets.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Semaphore

internal class ConnectionFactory(
    private val cfg: ReThisConfiguration,
    rootJob: Job,
) {
    private val logger = cfg.loggerFactory.get("eu.vendeli.rethis.core.ConnectionFactory")
    private val connections = Semaphore(cfg.maxConnections)
    private val scope = CoroutineScope(
        Dispatchers.IO_OR_UNCONFINED + CoroutineName("$CLIENT_NAME|ConnectionFactory") + Job(rootJob),
    )
    private val selector = SelectorManager(scope.coroutineContext)

    fun isReachedLimit() = connections.availablePermits == 0

    suspend fun createConnOrNull(address: SocketAddress): RConnection? {
        if (!connections.tryAcquire()) {
            logger.debug { "Creating connection attempt failed. Max connections reached." }
            return null
        }
        val conn = try {
            aSocket(selector)
                .tcp()
                .connect(address) {
                    cfg.socket.run {
                        timeout?.let { socketTimeout = it }
                        linger?.let { lingerSeconds = it }
                        this@connect.keepAlive = keepAlive
                        this@connect.noDelay = noDelay
                    }
                }.let { socket ->
                    cfg.tls?.let {
                        socket.tls(selector.coroutineContext, it)
                    } ?: socket
                }.rConnection().also {
                    prepareConnection(it)
                }
        } catch (e: Exception) {
            connections.release()
            throw e
        }

        return conn
    }

    suspend fun prepareConnection(conn: RConnection) {
        val cfgAuth = cfg.auth
        val cfgDb = cfg.db
        if (cfg.protocol != RespVer.V3 && cfgAuth == null && (cfgDb == null || cfgDb <= 0)) return

        val helloBuffer = HelloCommandCodec.encode(
            Charsets.UTF_8,
            cfg.protocol.literal.toLong(),
            cfgAuth?.let { HelloAuth(it.username ?: "default", it.password) },
            CLIENT_NAME.takeIf { cfg.pool.setClientName },
        ).buffer

        if (cfgDb != null && cfgDb > 0) {
            conn.doBatchRequest(
                listOf(
                    helloBuffer,
                    SelectCommandCodec.encode(Charsets.UTF_8, cfgDb.toLong()).buffer,
                ),
            )
        } else {
            conn.doRequest(helloBuffer)
        }
    }

    fun dispose(conn: RConnection) {
        connections.runCatching { release() }.onFailure {
            logger.debug("Failed to release connection", it)
        }
        conn.socket.dispose()
    }
}
