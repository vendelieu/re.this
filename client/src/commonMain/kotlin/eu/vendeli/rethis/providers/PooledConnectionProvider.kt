package eu.vendeli.rethis.providers

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.core.ConnectionPool
import eu.vendeli.rethis.types.common.Address
import eu.vendeli.rethis.types.common.RConnection
import eu.vendeli.rethis.utils.coRunBlocking
import kotlinx.io.Buffer

internal class PooledConnectionProvider(
    override val node: Address,
    private val client: ReThis,
) : ConnectionProvider() {
    private val pool = client.run {
        ConnectionPool(node.socket, cfg, connectionFactory, rootJob)
    }

    override suspend fun execute(request: CommandRequest): Buffer = withConnection { it.doRequest(request.buffer) }

    override fun close() {
        if (client.cfg.pool.closeGracefully) coRunBlocking { pool.closeGracefully() }
        else pool.close()
    }

    override suspend fun borrowConnection(): RConnection = pool.acquire()
    override suspend fun releaseConnection(conn: RConnection) = pool.release(conn)

    override fun hasSpareConnection() = pool.haveIdleConnections()
}
