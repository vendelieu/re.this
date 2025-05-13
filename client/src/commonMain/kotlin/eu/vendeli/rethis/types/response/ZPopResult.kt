package eu.vendeli.rethis.types.response

data class ZPopResult(
    val key: String,
    val popped: String,
    val score: Double,
)
