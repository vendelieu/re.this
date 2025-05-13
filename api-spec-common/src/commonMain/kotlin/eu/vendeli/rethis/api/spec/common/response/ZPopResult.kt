package eu.vendeli.rethis.api.spec.common.response

data class ZPopResult(
    val key: String,
    val popped: String,
    val score: Double,
)
