package eu.vendeli.rethis.api.spec.common.request.bitmap

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

sealed class BitcountOption

@RedisOption
class Range(
    val start: Long,
    val end: Long,
) : BitcountOption()

@RedisOption
enum class BitmapDataType {
    BYTE,
    BIT,
}
