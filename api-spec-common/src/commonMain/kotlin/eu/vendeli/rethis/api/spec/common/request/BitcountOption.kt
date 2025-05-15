package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

sealed class BitcountOption

@RedisOption
class Range(
    start: Long,
    end: Long,
) : BitcountOption()

@RedisOption
enum class BitmapDataType {
    BYTE,
    BIT,
}
