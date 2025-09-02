package eu.vendeli.rethis.shared.request.string

import eu.vendeli.rethis.shared.annotations.RedisOption

sealed class LcsOptions


sealed class LcsMode : LcsOptions() {
    data object LEN : LcsMode()
    data object IDX : LcsMode()
}

@RedisOption.Token("MINMATCHLEN")
class MinMatchLen(
    val minMatchLen: Long,
) : LcsOptions()
