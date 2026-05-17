package eu.vendeli.rethis.api.processor.types

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

/**
 * Mirrors the `vendelieu/redis-spec` artifact (`output/spec.json`). Field names match the
 * artifact verbatim (camelCase on the wire). Optional upstream fields stay nullable so the
 * deserializer never throws on minor schema additions.
 */
@Serializable
internal data class SpecBundle(
    @SerialName($$"$schemaVersion") val schemaVersion: String = "1.0.0",
    val manifest: SpecManifest,
    val commands: Map<String, RedisCommandApiSpec>,
    val indexes: SpecIndexes,
)

@Serializable
internal data class SpecManifest(
    val builtAt: String,
    val redisRepoSha: String? = null,
    val redisRepoTag: String? = null,
    val redisDocsSha: String? = null,
    val commandCount: Int = 0,
    val moduleCommandCount: Int = 0,
    val byModuleCount: Map<String, Int> = emptyMap(),
)

@Serializable
internal data class SpecIndexes(
    val byGroup: Map<String, List<String>> = emptyMap(),
    val byModule: Map<String, List<String>> = emptyMap(),
    val byContainer: Map<String, List<String>> = emptyMap(),
    val deprecated: List<String> = emptyList(),
    val blocking: List<String> = emptyList(),
)

@Serializable
internal data class RedisCommandApiSpec(
    val name: String = "",
    val container: String? = null,
    val summary: String = "",
    val since: String = "",
    val deprecatedSince: String? = null,
    val replacedBy: ReplacedBy? = null,
    val group: String = "",
    val module: String? = null,
    val complexity: String? = null,
    val arity: Int? = null,
    val commandFlags: List<String>? = null,
    val aclCategories: List<String>? = null,
    val commandTips: CommandTips = CommandTips(),
    val history: List<HistoryEntry> = emptyList(),
    val hints: List<String> = emptyList(),
    val docFlags: List<String> = emptyList(),
    val function: String? = null,
    val getKeysFunction: String? = null,
    val keySpecs: List<KeySpec>? = null,
    val arguments: List<CommandArgument> = emptyList(),
    val replies: Replies = Replies(),
) {
    val specTree: List<RSpecNode> by lazy { RSpecTreeBuilder.build(arguments) }

    val allNodes by lazy { collectAllNodes() }
    val allArguments: List<CommandArgument> by lazy { collectAllArguments() }

    private fun collectAllArguments(): List<CommandArgument> = arguments.flatMap {
        sequence {
            var stack = listOf(it)
            while (stack.isNotEmpty()) {
                val current = stack.first()
                stack = stack.drop(1)
                yield(current)
                stack = current.arguments + stack
            }
        }.toList()
    }

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
internal data class ReplacedBy(
    val command: String,
    val since: String? = null,
)

@Serializable
internal data class CommandTips(
    val nondeterministicOutput: Boolean = false,
    val nondeterministicOutputOrder: Boolean = false,
    val requestPolicy: String? = null,
    val responsePolicy: String? = null,
    val raw: List<String> = emptyList(),
)

@Serializable
internal data class HistoryEntry(
    val since: String,
    val note: String,
)

@Serializable
internal data class ArgumentSpec(
    val name: String,
    val type: String,
    val optional: Boolean = false,
    val multiple: Boolean = false,
    val multipleToken: Boolean? = null,
    val token: String? = null,
    val arguments: List<ArgumentSpec> = emptyList(),
)

@Serializable
internal data class CommandArgument(
    val name: String = "",
    val displayText: String? = null,
    val type: String = "unknown",
    val token: String? = null,
    val since: String? = null,
    val deprecatedSince: String? = null,
    val summary: String? = null,
    val optional: Boolean = false,
    val multiple: Boolean = false,
    val multipleToken: Boolean = false,
    val keySpecIndex: Int? = null,
    val arguments: List<CommandArgument> = emptyList(),
)

@Serializable
internal data class KeySpec(
    val notes: String? = null,
    val beginSearch: BeginSearch = BeginSearch(),
    val findKeys: FindKeys = FindKeys(),
    @SerialName("RW") val rW: Boolean = false,
    @SerialName("RO") val rO: Boolean = false,
    @SerialName("OW") val oW: Boolean = false,
    val access: Boolean = false,
    val update: Boolean = false,
    val insert: Boolean = false,
    val delete: Boolean = false,
    val incomplete: Boolean = false,
)

@Serializable
internal data class BeginSearch(
    val type: String = "unknown",
    val spec: BeginSearchSpec = BeginSearchSpec(),
)

@Serializable
internal data class BeginSearchSpec(
    val index: Int? = null,
    val keyword: String? = null,
    val startFrom: Int? = null,
)

@Serializable
internal data class FindKeys(
    val type: String = "unknown",
    val spec: FindKeysSpec = FindKeysSpec(),
)

@Serializable
internal data class FindKeysSpec(
    val lastKey: Int? = null,
    val keyStep: Int? = null,
    val limit: Int? = null,
    val firstKey: Int? = null,
    val keyNumIdx: Int? = null,
)

@Serializable
internal data class Replies(
    val resp2: ReplyShape? = null,
    val resp3: ReplyShape? = null,
    val protocolNotes: ProtocolNotes = ProtocolNotes(),
    val rawText: ReplyRawText = ReplyRawText(),
    val sources: List<String> = emptyList(),
    val confidence: ReplyConfidence = ReplyConfidence(),
)

@Serializable
internal data class ReplyConfidence(
    val resp2: String? = null,
    val resp3: String? = null,
)

@Serializable
internal data class ProtocolNotes(
    val differs: Boolean = false,
    val summary: String? = null,
)

@Serializable
internal data class ReplyRawText(
    val resp2: String? = null,
    val resp3: String? = null,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("kind")
internal sealed interface ReplyShape {
    val description: String?
}

@Serializable
@SerialName("simpleString")
internal data class SimpleStringReply(
    val value: String? = null,
    override val description: String? = null,
) : ReplyShape

@Serializable
@SerialName("simpleError")
internal data class SimpleErrorReply(
    override val description: String? = null,
) : ReplyShape

@Serializable
@SerialName("integer")
internal data class IntegerReply(
    val minimum: Long? = null,
    val maximum: Long? = null,
    override val description: String? = null,
) : ReplyShape

@Serializable
@SerialName("double")
internal data class DoubleReply(
    override val description: String? = null,
) : ReplyShape

@Serializable
@SerialName("boolean")
internal data class BooleanReply(
    override val description: String? = null,
) : ReplyShape

@Serializable
@SerialName("bulkString")
internal data class BulkStringReply(
    val encoding: String? = null,
    override val description: String? = null,
) : ReplyShape

@Serializable
@SerialName("verbatimString")
internal data class VerbatimStringReply(
    val format: String? = null,
    override val description: String? = null,
) : ReplyShape

@Serializable
@SerialName("bigNumber")
internal data class BigNumberReply(
    override val description: String? = null,
) : ReplyShape

@Serializable
@SerialName("null")
internal data class NullReply(
    override val description: String? = null,
) : ReplyShape

@Serializable
@SerialName("array")
internal data class ArrayReply(
    val items: ReplyShape,
    val minItems: Long? = null,
    val maxItems: Long? = null,
    override val description: String? = null,
) : ReplyShape

@Serializable
@SerialName("set")
internal data class SetReply(
    val items: ReplyShape,
    override val description: String? = null,
) : ReplyShape

@Serializable
@SerialName("map")
internal data class MapReply(
    val key: ReplyShape,
    val value: ReplyShape,
    override val description: String? = null,
) : ReplyShape

@Serializable
@SerialName("tuple")
internal data class TupleReply(
    val items: List<ReplyShape> = emptyList(),
    override val description: String? = null,
) : ReplyShape

@Serializable
@SerialName("oneOf")
internal data class OneOfReply(
    val variants: List<ReplyShape> = emptyList(),
    override val description: String? = null,
) : ReplyShape

@Serializable
@SerialName("push")
internal data class PushReply(
    val items: List<ReplyShape> = emptyList(),
    override val description: String? = null,
) : ReplyShape

@Serializable
@SerialName("unknown")
internal data class UnknownReply(
    val rawText: String,
    override val description: String? = null,
) : ReplyShape
