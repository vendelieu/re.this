package eu.vendeli.rethis.shared.response.sortedset

data class ZPopResult(
    val key: String,
    val popped: String,
    val score: Double,
)
