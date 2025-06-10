package eu.vendeli.rethis.api.processor.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CommandArgument(
    val name: String,
    @SerialName("display_text") val displayText: String? = null,
    val type: String,
    val optional: Boolean = false,
    val multiple: Boolean = false,
    @SerialName("multiple_token") val multipleToken: Boolean = false,
    val token: String? = null,
    @SerialName("key_spec_index") val keySpecIndex: Int? = null,
    val arguments: List<CommandArgument> = emptyList(),
)
