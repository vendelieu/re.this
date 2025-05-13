package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

sealed class ClusterSetSlotOption {
    @RedisOption
    data class IMPORTING(val nodeId: String) : ClusterSetSlotOption()

    @RedisOption
    data class MIGRATING(val nodeId: String) : ClusterSetSlotOption()

    @RedisOption
    data class NODE(val nodeId: String) : ClusterSetSlotOption()

    @RedisOption
    data object STABLE : ClusterSetSlotOption()
}
