package eu.vendeli.rethis.providers

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.core.withAcquireTimeout
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.types.common.Address
import eu.vendeli.rethis.types.common.RConnection
import kotlinx.io.Buffer
import kotlinx.io.IOException

internal class SingleConnectionProvider(
    override val node: Address,
    private val client: ReThis,
) : ConnectionProvider() {
    override suspend fun execute(request: CommandRequest): Buffer =
        withConnection { it.doRequest(request.data, commandTimeout = client.cfg.commandTimeoutFor(request)) }

    override fun close() {}

    override suspend fun borrowConnection(): RConnection = withAcquireTimeout(client.cfg.connectionAcquireTimeout) {
        client.connectionFactory.createConnOrNull(node.socket) ?: throw IOException("Can't create connection to $node")
    }

    override suspend fun releaseConnection(conn: RConnection) = client.connectionFactory.dispose(conn)
    override fun disposeConnection(conn: RConnection) = client.connectionFactory.dispose(conn)

    override fun hasSpareConnection() = !client.connectionFactory.isReachedLimit()
}
