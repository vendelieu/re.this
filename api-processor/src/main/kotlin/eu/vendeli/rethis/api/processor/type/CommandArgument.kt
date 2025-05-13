package eu.vendeli.rethis.api.processor.type

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CommandArgument(
    val name: String,
    val type: String,
    val optional: Boolean = false,
    val multiple: Boolean = false,
    val token: String? = null,
    @SerialName("key_spec_index") val keySpecIndex: Int? = null,
    val arguments: List<CommandArgument> = emptyList()
)
