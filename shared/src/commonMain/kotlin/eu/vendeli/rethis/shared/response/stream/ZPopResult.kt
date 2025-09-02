package eu.vendeli.rethis.shared.response.stream

data class ZPopResult(
    val key: String,
    val popped: String,
    val score: Double,
)
