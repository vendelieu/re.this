package eu.vendeli.rethis.providers

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.ReThisException
import eu.vendeli.rethis.types.common.Address
import eu.vendeli.rethis.types.common.RConnection
import kotlinx.coroutines.withTimeout
import kotlinx.io.Buffer

internal class SingleConnectionProvider(
    override val node: Address,
    private val client: ReThis,
) : ConnectionProvider() {
    override suspend fun execute(request: CommandRequest): Buffer = withConnection { it.doRequest(request.buffer) }

    override fun close() {}

    override suspend fun borrowConnection(): RConnection = withTimeout(client.cfg.connectionAcquireTimeout) {
        client.connectionFactory.createConnOrNull(node.socket) ?: throw ReThisException("Can't create connection")
    }

    override suspend fun releaseConnection(conn: RConnection) = client.connectionFactory.dispose(conn)

    override fun hasSpareConnection() = !client.connectionFactory.isReachedLimit()
}
