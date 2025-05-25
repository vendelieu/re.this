package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer

@RedisOptionContainer
sealed class ClusterSetSlotOption {
    @RedisOption.Token("IMPORTING")
    data class Importing(val nodeId: String) : ClusterSetSlotOption()

    @RedisOption.Token("MIGRATING")
    data class Migrating(val nodeId: String) : ClusterSetSlotOption()

    @RedisOption.Token("NODE")
    data class Node(val nodeId: String) : ClusterSetSlotOption()

    @RedisOption
    data object STABLE : ClusterSetSlotOption()
}
