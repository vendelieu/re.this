package eu.vendeli.rethis.shared.request.server

import eu.vendeli.rethis.shared.annotations.RedisOption

sealed class SflushOption {
    @RedisOption.Token("SYNC")
    data object Sync : SflushOption()

    @RedisOption.Token("ASYNC")
    data object Async : SflushOption()

    class SlotRange(
        @RedisOption.Name("slot-start") val startSlot: Long,
        @RedisOption.Name("slot-last") val endSlot: Long,
    ) : SflushOption()

    class Other(vararg val args: String) : SflushOption()
}
