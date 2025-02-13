package eu.vendeli.rethis.types.response

data class PubSubNumEntry(
    val name: String,
    val subscribersCount: Long,
)
