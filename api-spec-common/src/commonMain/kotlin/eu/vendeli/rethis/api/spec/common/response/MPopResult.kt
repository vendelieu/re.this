package eu.vendeli.rethis.api.spec.common.response

data class MPopResult(
    val name: String,
    val poppedElements: List<String>,
)
