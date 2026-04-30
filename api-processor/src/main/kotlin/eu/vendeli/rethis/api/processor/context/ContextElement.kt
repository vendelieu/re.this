package eu.vendeli.rethis.api.processor.context

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import eu.vendeli.rethis.api.processor.core.RedisCommandProcessor.Companion.context
import eu.vendeli.rethis.api.processor.types.*
import eu.vendeli.rethis.api.processor.utils.*
import eu.vendeli.rethis.shared.types.RespCode

internal interface ContextElement {
    fun onFinish() {}
    val key: ContextKey<out ContextElement>
}

internal sealed interface ContextKey<E : ContextElement> {
    val isPerCommand: Boolean
        get() = false
}

internal class RSpecRaw(val specs: Map<String, RedisCommandApiSpec>) : ContextElement {
    override val key = RSpecRaw

    companion object : ContextKey<RSpecRaw>
}

internal class ProcessorMeta(
    val codeGenerator: CodeGenerator,
    val codecsPackage: String,
    val commandPackage: String,
) : ContextElement {
    override val key = ProcessorMeta

    companion object : ContextKey<ProcessorMeta>
}

internal class Logger(val logger: KSPLogger) : ContextElement {
    override val key = Logger

    companion object : ContextKey<Logger>
}

internal class ResolvedSpecs(val spec: Map<RCommandData, List<KSClassDeclaration>>) : ContextElement {
    override val key = ResolvedSpecs

    companion object : ContextKey<ResolvedSpecs>
}

internal class SpecResponses(
    val responses: Map<String, Set<eu.vendeli.rethis.shared.types.RespCode>>,
) : ContextElement {
    override val key = SpecResponses
    private val processedResponses = mutableMapOf<String, MutableSet<eu.vendeli.rethis.shared.types.RespCode>>()

    fun addProcessedResponses(command: String, rTypes: List<eu.vendeli.rethis.shared.types.RespCode>) {
        val container = processedResponses.getOrPut(command) { mutableSetOf() }
        container += rTypes
    }

    companion object : ContextKey<SpecResponses>
}

internal class ValidationResult() : ContextElement {
    override val key = ValidationResult
    private val errors = mutableMapOf<String, MutableList<String>>()

    fun reportError(command: String, message: String) {
        val container = errors.getOrPut(command) { mutableListOf() }
        container += message
    }

    fun getErrors(): Map<String, List<String>> = errors.filterValues { it.isNotEmpty() }

    companion object : ContextKey<ValidationResult>
}

internal class CommandFileSpec(
    val commandFile: MutableMap<String, Entry> = mutableMapOf(),
) : ContextElement {
    override val key = CommandFileSpec

    internal class Entry(
        val builder: FileSpec.Builder,
        val originatingFiles: MutableSet<KSFile> = mutableSetOf(),
    )

    fun getFor(command: String): FileSpec.Builder {
        val normalizedKey = command.uppercase().trim()
        val cmdPackagePart = "." + context.currentCommand.klass.packageName.asString().substringAfterLast(".")
        val fullKey = "$normalizedKey|$cmdPackagePart"

        val entry = commandFile.getOrPut(fullKey) {
            val fileName = context.currentCommand.command.name.toPascalCase()
            Entry(
                FileSpec.builder(
                    context.meta.commandPackage + cmdPackagePart,
                    fileName,
                ).indent(" ".repeat(4)),
            )
        }
        context.currentCommand.klass.containingFile?.let(entry.originatingFiles::add)
        return entry.builder
    }

    override fun onFinish() {
        val codeGenerator = context.meta.codeGenerator
        commandFile.values.forEach { entry ->
            val spec = entry.builder.build()
            runCatching {
                codeGenerator.createNewFile(
                    Dependencies(aggregating = true, *entry.originatingFiles.toTypedArray()),
                    spec.packageName,
                    spec.name,
                ).bufferedWriter().use { spec.writeTo(it) }
            }.onFailure { it.printStackTrace() }
        }
    }

    companion object : ContextKey<CommandFileSpec>
}

internal class CodecFileSpec(val fileSpec: FileSpec.Builder) : ContextElement {
    override val key = CodecFileSpec

    companion object : ContextKey<CodecFileSpec> {
        override val isPerCommand = true
    }
}

internal class CodecObjectTypeSpec(val typeSpec: TypeSpec.Builder) : ContextElement {
    override val key = CodecObjectTypeSpec

    companion object : ContextKey<CodecObjectTypeSpec> {
        override val isPerCommand = true
    }
}

internal class CurrentCommand(val command: RCommandData, val klass: KSClassDeclaration) : ContextElement {
    override val key = CurrentCommand

    val encodeFunction: KSFunctionDeclaration = klass.declarations
        .filterIsInstance<KSFunctionDeclaration>()
        .first { it.simpleName.asString() == "encode" }

    val hasCustomEncoder = klass.hasCustomEncoder()
    val hasCustomDecoder = klass.hasCustomDecoder()

    val customCodec = klass.runCatching { getCustom() }.getOrNull()

    val specType = klass.superTypes.first().resolve().arguments.first() // RedisCommandSpec<T>
    val type = specType.toTypeName()
    val arguments = specType.type?.resolve()?.let { t ->
        t.takeIf {
            it.arguments.isNotEmpty()
        }?.arguments?.map { it.type?.resolve() } ?: listOf(t)
    }?.filterNotNull()?.toTypedArray()!!

    val haveVaryingSize by lazy {
        context.enrichedTree.children.any { ch ->
            ch.attr.any { attr ->
                attr.safeCast<EnrichedTreeAttr.Multiple>()?.let { it.vararg || it.collection } == true ||
                    attr.safeCast<EnrichedTreeAttr.Optional>()?.local == OptionalityType.Nullable
            }
        }
    }

    companion object : ContextKey<CurrentCommand> {
        override val isPerCommand = true
    }
}

internal class Imports(val imports: MutableSet<String> = mutableSetOf()) : ContextElement {
    override val key = Imports

    companion object : ContextKey<Imports> {
        override val isPerCommand = true
    }
}

internal class ETree(val tree: EnrichedNode) : ContextElement {
    override val key = ETree

    companion object : ContextKey<ETree> {
        override val isPerCommand = true
    }
}


internal class CollectedTokens(
    val tokens: MutableSet<String> = mutableSetOf()
) : ContextElement {
    override val key = CollectedTokens

    fun addToken(tokenName: String) {
        tokens.add(tokenName)
    }

    override fun onFinish() {
        context.logger.warn("CollectedTokens.onFinish() called with ${tokens.size} tokens")
        if (tokens.isEmpty()) {
            context.logger.warn("No tokens collected, skipping RedisTokens generation")
            return
        }

        val sortedTokens = tokens.sorted()
        val fileContent = buildString {
            appendLine("package eu.vendeli.rethis.utils")
            appendLine()
            appendLine("/**")
            appendLine(" * Predefined byte arrays for Redis command tokens.")
            appendLine(" * Auto-generated from @RedisOption.Token annotations.")
            appendLine(" * These are computed once to avoid repeated string-to-byte conversions in generated codecs.")
            appendLine(" */")
            appendLine("internal object RedisToken {")

            sortedTokens.forEach { token ->
                val propertyName = tokenToRedisTokenPropertyName(token)
                val kotlinString = token.replace("\"", "\\\"")
                appendLine("    val $propertyName = \"$kotlinString\".encodeToByteArray()")
            }

            appendLine("}")
        }

        context.meta.codeGenerator.createNewFile(
            Dependencies.ALL_FILES,
            "eu.vendeli.rethis.utils",
            "RedisToken",
        ).bufferedWriter().use { it.write(fileContent) }
        context.logger.warn("Generated RedisToken.kt with ${tokens.size} tokens")
    }

    companion object : ContextKey<CollectedTokens>
}

internal class CodeGenContext(
    val builder: CodeBlock.Builder,
    private var nameCtr: Int = 0,
) : ContextElement {
    private var thisExpr: String = ""
    override val key = CodeGenContext
    val blockStack = ArrayDeque<Pair<BlockType, String>>()

    private fun freshName(): String {
        val newName = "it" + nameCtr++
        thisExpr = newName
        return newName
    }

    fun buildBlock(fieldName: String, type: BlockType, parentPointer: String? = null, block: () -> Unit) {
        val oldPointer = thisExpr
        thisExpr = when (type) {
            BlockType.WHEN if thisExpr.isNotBlank() -> thisExpr
            BlockType.WHEN -> fieldName
            else -> freshName()
        }
        // before block should be old pointer or param

        val prevName = blockStack.lastOrNull()?.second
        // Use explicit parentPointer if provided, otherwise use oldPointer only if we're nested
        val effectivePointer = parentPointer ?: oldPointer.takeIf { blockStack.isNotEmpty() } ?: ""
        val newName = pointedParameter(fieldName, effectivePointer)
        val paramPointer = if (prevName != fieldName && newName.substringAfter('.') != fieldName) ".$fieldName" else ""

        val guard = when (type) {
            BlockType.LET -> "${newName}$paramPointer?.let { $thisExpr ->"
            BlockType.FOR -> "${newName}$paramPointer.forEach { $thisExpr ->"
            BlockType.WHEN -> "when (${if (effectivePointer.isNotBlank()) effectivePointer else fieldName}$paramPointer)"
        }

        blockStack.addLast(type to fieldName)
        builder.beginControlFlow(guard)
        block()
        builder.endControlFlow()
        require(blockStack.removeLast().first == type)

        thisExpr = oldPointer
    }


    fun appendLine(line: String, vararg args: Any?) {
        builder.addStatement(line, *args)
    }

    var pointer: String?
        get() = thisExpr.takeIf { it.isNotBlank() }
        set(value) {
            thisExpr = value ?: ""
        }

    fun pointedParameter(name: String, pointer: String = thisExpr, isComplex: Boolean = false): String {
        val parameter = mutableListOf<String?>()
        when {
            blockStack.isEmpty() || blockStack.singleOrNull()?.first == BlockType.WHEN -> {
                parameter.add(pointer.takeIf { it.isNotBlank() })
                parameter.add(name)
            }

            blockStack.last().first != BlockType.WHEN -> parameter.add(pointer)
            else -> {
                parameter.add(pointer.takeIf { it.isNotBlank() })
                parameter.add(name)
            }
        }
        if (isComplex && parameter.last() != name) parameter.add(name)

        return parameter.filterNotNull().joinToString(".")
    }

    enum class BlockType {
        WHEN, LET, FOR
    }

    companion object : ContextKey<CodeGenContext> {
        override val isPerCommand = true
    }
}
