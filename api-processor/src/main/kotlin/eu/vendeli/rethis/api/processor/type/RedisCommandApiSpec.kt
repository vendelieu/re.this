package eu.vendeli.rethis.api.processor.type

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RedisCommandApiSpec(
    val summary: String,
    val since: String,
    val group: String,
    @SerialName("command_flags") val commandFlags: List<String>? = null,
    @SerialName("acl_categories") val aclCategories: List<String>? = null,
    val arguments: List<CommandArgument>? = null,
    @SerialName("key_specs") val keySpecs: List<KeySpec>? = null,
    val complexity: String? = null,
    val arity: Int? = null,
)
