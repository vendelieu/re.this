package eu.vendeli.rethis.api.spec.common.response

data class PubSubNumEntry(
    val name: String,
    val subscribersCount: Long,
)
