package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.PairArgument

sealed class LcsOptions

sealed class LcsMode : LcsOptions() {
    data object LEN : LcsMode()
    data object IDX : LcsMode()
}

data class MinMatchLen(
    val length: Long,
) : LcsOptions(),
    PairArgument<String, Long> {
    override val arg = "MINMATCHLEN" to length
}

data object WITHMATCHLEN : LcsOptions()
