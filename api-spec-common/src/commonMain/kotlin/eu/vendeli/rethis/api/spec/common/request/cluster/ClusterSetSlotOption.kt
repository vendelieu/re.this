package eu.vendeli.rethis.api.spec.common.request.cluster

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption


sealed class ClusterSetSlotOption {
    @RedisOption.Token("IMPORTING")
    class Importing(@RedisOption.Name("importing") val nodeId: String) : ClusterSetSlotOption()

    @RedisOption.Token("MIGRATING")
    class Migrating(@RedisOption.Name("migrating") val nodeId: String) : ClusterSetSlotOption()

    @RedisOption.Token("NODE")
    class Node(@RedisOption.Name("node") val nodeId: String) : ClusterSetSlotOption()

    data object STABLE : ClusterSetSlotOption()
}
