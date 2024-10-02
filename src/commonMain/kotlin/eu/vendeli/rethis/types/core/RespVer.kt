package eu.vendeli.rethis.types.core

sealed class RespVer(
    val literal: Short,
) {
    data object V2 : RespVer(2)
    data object V3 : RespVer(3)
}
