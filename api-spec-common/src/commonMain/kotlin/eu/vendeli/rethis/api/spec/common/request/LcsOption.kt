package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer

sealed class LcsOptions

@RedisOptionContainer
sealed class LcsMode : LcsOptions() {
    @RedisOption
    data object LEN : LcsMode()
    @RedisOption
    data object IDX : LcsMode()
}

@RedisOption.Name("MINMATCHLEN")
class MinMatchLen(
    val length: Long,
) : LcsOptions()

@RedisOption
data object WITHMATCHLEN : LcsOptions()
