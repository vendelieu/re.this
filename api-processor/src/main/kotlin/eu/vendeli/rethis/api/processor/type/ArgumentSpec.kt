package eu.vendeli.rethis.api.processor.type

import kotlinx.serialization.Serializable

@Serializable
internal data class ArgumentSpec(
    val name: String,
    val type: String,
    val optional: Boolean = false,
    val multiple: Boolean = false,
    val multiple_token: Boolean? = null,
    val token: String? = null,
    val arguments: List<ArgumentSpec> = emptyList()
)
