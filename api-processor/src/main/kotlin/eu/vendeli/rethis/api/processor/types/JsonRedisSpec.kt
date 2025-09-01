package eu.vendeli.rethis.api.processor.types

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
) {
    val specTree: List<RSpecNode> by lazy { RSpecTreeBuilder.build(arguments.orEmpty()) }

    val allNodes by lazy { collectAllNodes() }
    val allArguments: List<CommandArgument> by lazy { collectAllArguments() }

    private fun collectAllArguments(): List<CommandArgument> = arguments?.flatMap {
        sequence {
            var stack = listOf(it)
            while (stack.isNotEmpty()) {
                val current = stack.first()
                stack = stack.drop(1)
                yield(current)
                stack = current.arguments + stack
            }
        }.toList()
    }.orEmpty()

    private fun collectAllNodes(): List<RSpecNode> = specTree.flatMap {
        sequence {
            var stack = listOf(it)
            while (stack.isNotEmpty()) {
                val current = stack.first()
                stack = stack.drop(1)
                yield(current)
                stack = current.children + stack
            }
        }.toList()
    }
}

@Serializable
internal data class ArgumentSpec(
    val name: String,
    val type: String,
    val optional: Boolean = false,
    val multiple: Boolean = false,
    @SerialName("multiple_token") val multipleToken: Boolean? = null,
    val token: String? = null,
    val arguments: List<ArgumentSpec> = emptyList()
)

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

@Serializable
internal data class KeySpec(
    @SerialName("begin_search") val beginSearch: BeginSearch? = null,
    @SerialName("find_keys") val findKeys: FindKeys? = null,
    @SerialName("RW") val rW: Boolean? = null,
    @SerialName("RO") val rO: Boolean? = null,
    val insert: Boolean? = null,
    val access: Boolean? = null,
    val delete: Boolean? = null
)

@Serializable
internal data class BeginSearch(
    val type: String,
    val spec: BeginSearchSpec? = null
)

@Serializable
internal data class BeginSearchSpec(
    val index: Int? = null
)

@Serializable
internal data class FindKeys(
    val type: String,
    val spec: FindKeysSpec
)

@Serializable
internal data class FindKeysSpec(
    @SerialName("lastkey") val lastKey: Int? = null,
    @SerialName("keystep") val keyStep: Int? = null,
    val limit: Int? = null
)
