package eu.vendeli.rethis.types.common

sealed class RespVer(
    val literal: Int,
) {
    data object V2 : RespVer(2)
    data object V3 : RespVer(3)
}
