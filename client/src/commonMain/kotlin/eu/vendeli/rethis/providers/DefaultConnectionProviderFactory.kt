package eu.vendeli.rethis.providers

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.common.Address

class DefaultConnectionProviderFactory(
    private val client: ReThis,
) : ConnectionProviderFactory {
    override fun create(node: Address): ConnectionProvider = if (client.cfg.usePooling) {
        PooledConnectionProvider(node, client)
    } else {
        SingleConnectionProvider(node, client)
    }
}
