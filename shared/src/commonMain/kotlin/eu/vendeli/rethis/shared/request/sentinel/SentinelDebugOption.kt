package eu.vendeli.rethis.shared.request.sentinel

sealed class SentinelDebugOption {
    class Param(val name: String, val value: Long) : SentinelDebugOption()

    class Other(vararg val args: String) : SentinelDebugOption()
}
