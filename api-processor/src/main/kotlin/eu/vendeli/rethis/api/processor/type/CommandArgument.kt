package eu.vendeli.rethis.api.processor.type

import eu.vendeli.rethis.api.processor.utils.NameNormalizer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CommandArgument(
    val name: String,
    @SerialName("display_text") val displayText: String? = null,
    val type: String,
    val optional: Boolean = false,
    val multiple: Boolean = false,
    val token: String? = null,
    @SerialName("key_spec_index") val keySpecIndex: Int? = null,
    val arguments: List<CommandArgument> = emptyList(),
) {
    val specName get() = displayText ?: name
    val normalizedName get() = NameNormalizer.normalizeParam(specName)
}
