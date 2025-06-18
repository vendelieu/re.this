package eu.vendeli.rethis.api.spec.common.request.string

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

sealed class LcsOptions


sealed class LcsMode : LcsOptions() {
    data object LEN : LcsMode()
    data object IDX : LcsMode()
}

@RedisOption.Token("MINMATCHLEN")
class MinMatchLen(
    val minMatchLen: Long,
) : LcsOptions()
