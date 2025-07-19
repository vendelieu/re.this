package eu.vendeli.rethis.api.spec.common.response.common

data class MPopResult(
    val name: String,
    val poppedElements: List<String>,
)
