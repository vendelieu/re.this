package eu.vendeli.rethis.shared.request.sentinel

import eu.vendeli.rethis.shared.annotations.RedisOption

sealed class SentinelDebugOption {
    class Param(
        @RedisOption.Name("parameter") val name: String,
        val value: Long,
    ) : SentinelDebugOption()

    class Other(vararg val args: String) : SentinelDebugOption()
}
