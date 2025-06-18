package eu.vendeli.rethis.api.spec.common.request.bitmap

sealed class BitcountOption

class Range(
    val start: Long,
    val end: Long,
) : BitcountOption()

enum class BitmapDataType {
    BYTE,
    BIT,
}
