package eu.vendeli.rethis.providers

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.core.ConnectionPool
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.types.common.Address
import eu.vendeli.rethis.types.common.RConnection
import eu.vendeli.rethis.utils.BlockingUnsupportedException
import eu.vendeli.rethis.utils.coRunBlocking
import kotlinx.io.Buffer

internal class PooledConnectionProvider(
    override val node: Address,
    private val client: ReThis,
) : ConnectionProvider() {
    private val pool = client.run {
        ConnectionPool(node.socket, cfg, connectionFactory, rootJob)
    }

    override suspend fun execute(request: CommandRequest): Buffer =
        withConnection { it.doRequest(request.data, commandTimeout = client.cfg.commandTimeoutFor(request)) }

    override fun close() {
        if (!client.cfg.pool.closeGracefully) return pool.close()
        try {
            coRunBlocking { pool.closeGracefully() }
        } catch (_: BlockingUnsupportedException) {
            // Targets that cannot block (JS, wasmJs) close the pool immediately instead.
            pool.close()
        }
    }

    override suspend fun borrowConnection(): RConnection = pool.acquire()
    override suspend fun releaseConnection(conn: RConnection) = pool.release(conn)
    override fun disposeConnection(conn: RConnection) = client.connectionFactory.dispose(conn)

    override fun hasSpareConnection() = pool.haveIdleConnections()
}
