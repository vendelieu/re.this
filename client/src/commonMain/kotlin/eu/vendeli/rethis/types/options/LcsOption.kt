package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.common.VaryingArgument
import eu.vendeli.rethis.types.common.toArgument

sealed class LcsOptions

sealed class LcsMode : LcsOptions() {
    data object LEN : LcsMode()
    data object IDX : LcsMode()
}

class MinMatchLen(
    length: Long,
) : LcsOptions(),
    VaryingArgument {
    override val data = listOf("MINMATCHLEN".toArgument(), length.toArgument())
}

data object WITHMATCHLEN : LcsOptions()
