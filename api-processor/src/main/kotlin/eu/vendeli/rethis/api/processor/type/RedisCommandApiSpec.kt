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

internal fun RedisCommandApiSpec.collectAllArguments(): List<CommandArgument> = arguments?.flatMap {
    sequence {
        var stack = listOf(it)
        while (stack.isNotEmpty()) {
            val current = stack.first()
            stack = stack.drop(1)
            yield(current)
            stack = current.arguments + stack
        }
    }.toList()
} ?: emptyList()
