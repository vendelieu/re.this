package eu.vendeli.rethis.providers

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.core.ConnectionPool
import eu.vendeli.rethis.types.common.RConnection
import eu.vendeli.rethis.utils.withConnection
import eu.vendeli.rethis.utils.withRetry
import io.ktor.network.sockets.*
import kotlinx.io.Buffer

internal class StandaloneProvider(
    address: SocketAddress,
    private val client: ReThis,
) : ConnectionProvider() {
    private val pool = ConnectionPool(address, client)

    override suspend fun execute(request: CommandRequest): Buffer = withRetry(client.cfg.retry) {
        pool.withConnection {
            it.doRequest(request.buffer)
        }
    }

    override fun close() {
        pool.close()
    }

    override suspend fun closeGracefully() {
        pool.closeGracefully()
    }

    override suspend fun borrowConnection(slot: Int?): RConnection = pool.acquire()
    override suspend fun releaseConnection(conn: RConnection) = client.connectionFactory.dispose(conn)
}
