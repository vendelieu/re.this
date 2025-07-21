package eu.vendeli.rethis.topology

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.types.common.Address

internal class StandaloneTopologyManager(
    node: Address,
    client: ReThis,
) : TopologyManager {
    override val cfg = client.cfg
    internal val provider = client.connectionProviderFactory.create(node)

    override suspend fun route(request: CommandRequest): ConnectionProvider = provider

    override fun close() = provider.close()
}
