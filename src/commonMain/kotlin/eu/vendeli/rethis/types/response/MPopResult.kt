package eu.vendeli.rethis.types.response

data class MPopResult(
    val name: String,
    val poppedElements: List<String>,
)
