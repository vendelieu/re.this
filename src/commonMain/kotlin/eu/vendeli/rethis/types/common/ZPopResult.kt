package eu.vendeli.rethis.types.common

data class ZPopResult(
    val key: String,
    val popped: String,
    val score: Double,
)
