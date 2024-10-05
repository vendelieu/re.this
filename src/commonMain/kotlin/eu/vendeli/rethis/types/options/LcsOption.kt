package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArg

sealed class LcsOptions

sealed class LcsMode : LcsOptions() {
    data object LEN : LcsMode()
    data object IDX : LcsMode()
}

data class MinMatchLen(
    val length: Long,
) : LcsOptions(),
    VaryingArgument {
    override val data = listOf("MINMATCHLEN".toArg(), length.toArg())
}

data object WITHMATCHLEN : LcsOptions()
