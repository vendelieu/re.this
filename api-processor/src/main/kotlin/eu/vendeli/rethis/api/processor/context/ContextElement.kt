package eu.vendeli.rethis.api.processor.context

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import eu.vendeli.rethis.api.processor.core.RedisCommandProcessor.Companion.context
import eu.vendeli.rethis.api.processor.types.*
import eu.vendeli.rethis.api.processor.utils.enrichedTree
import eu.vendeli.rethis.api.processor.utils.hasCustomDecoder
import eu.vendeli.rethis.api.processor.utils.hasCustomEncoder
import eu.vendeli.rethis.api.processor.utils.safeCast
import eu.vendeli.rethis.api.spec.common.types.RespCode

internal interface ContextElement {
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
    val clientDir: String,
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

internal class ResolvedSpecs(val spec: Map<RCommandData, KSClassDeclaration>) : ContextElement {
    override val key = ResolvedSpecs
    val groupedSpecs by lazy { spec.entries.groupBy { it.key.name } }

    companion object : ContextKey<ResolvedSpecs>
}

internal class SpecResponses(
    val responses: Map<String, Set<RespCode>>,
) : ContextElement {
    override val key = SpecResponses
    private val processedResponses = mutableMapOf<String, MutableSet<RespCode>>()

    fun addProcessedResponses(command: String, rTypes: List<RespCode>) {
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

    val specType = klass.superTypes.first().resolve().arguments.first() // RedisCommandSpec<T>
    val type = specType.toTypeName()

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

    fun buildBlock(fieldName: String, type: BlockType, block: () -> Unit) {
        val oldPointer = thisExpr
        thisExpr = when {
            type == BlockType.WHEN && thisExpr.isNotBlank() -> thisExpr
            type == BlockType.WHEN -> fieldName
            else -> freshName()
        }
        // before block should be old pointer or param

        val prevName = blockStack.lastOrNull()?.second
        val newName = pointedParameter(fieldName, oldPointer)
        val paramPointer = if (prevName != fieldName && newName.substringAfter('.') != fieldName) ".$fieldName" else ""

        val guard = when (type) {
            BlockType.LET -> "${newName}$paramPointer?.let { $thisExpr ->"
            BlockType.FOR -> "${newName}$paramPointer.forEach { $thisExpr ->"
            BlockType.WHEN -> "when (${pointer ?: fieldName}$paramPointer)"
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

    val pointer: String?
        get() = thisExpr.takeIf { !it.isBlank() }

    fun pointedParameter(name: String, pointer: String = thisExpr): String {
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

        return parameter.filterNotNull().joinToString(".")
    }

    enum class BlockType {
        WHEN, LET, FOR
    }

    companion object : ContextKey<CodeGenContext> {
        override val isPerCommand = true
    }
}
