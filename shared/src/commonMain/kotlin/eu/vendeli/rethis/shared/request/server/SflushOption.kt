package eu.vendeli.rethis.shared.request.server

import eu.vendeli.rethis.shared.annotations.RedisOption

sealed class SflushOption {
    @RedisOption.Token("SYNC")
    data object Sync : SflushOption()

    @RedisOption.Token("ASYNC")
    data object Async : SflushOption()

    class SlotRange(val startSlot: Long, val endSlot: Long) : SflushOption()

    class Other(vararg val args: String) : SflushOption()
}
