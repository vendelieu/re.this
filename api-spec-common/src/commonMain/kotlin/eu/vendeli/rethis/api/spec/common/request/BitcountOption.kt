package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

sealed class BitcountOption {

}

@RedisOption.SkipName
class Range(
    start: Int,
    end: Int,
) : BitcountOption()

@RedisOption
enum class BitmapDataType {
    BYTE,
    BIT,
}
