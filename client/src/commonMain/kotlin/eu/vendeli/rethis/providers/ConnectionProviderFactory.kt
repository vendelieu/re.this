package eu.vendeli.rethis.providers

import eu.vendeli.rethis.types.common.Address

interface ConnectionProviderFactory {
    /**
     * Create a provider for a single node.
     * Might return a pooled or a “single‐use” provider depending on cfg.
     */
    fun create(node: Address): ConnectionProvider

    /**
     * If your topology needs one provider per node,
     * this helper spins up a map of [node]→[provider].
     */
    fun createAll(nodes: Collection<Address>): Map<Address, ConnectionProvider> =
        nodes.associateWith { create(it) }
}
