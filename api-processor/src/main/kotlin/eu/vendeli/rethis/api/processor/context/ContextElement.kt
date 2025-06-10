package eu.vendeli.rethis.api.processor.context

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import eu.vendeli.rethis.api.processor.types.*
import eu.vendeli.rethis.api.processor.utils.hasCustomDecoder
import eu.vendeli.rethis.api.processor.utils.hasCustomEncoder
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

internal class LibSpecTree(val tree: LibSpecNode) : ContextElement {
    override val key = LibSpecTree

    companion object : ContextKey<LibSpecTree> {
        override val isPerCommand = true
    }
}

internal class CodecMeta(
    val codecPackage: String,
    val codecName: String,
) : ContextElement {
    override val key = CodecMeta

    companion object : ContextKey<CodecMeta> {
        override val isPerCommand = true
    }
}

internal class CodecExtensions(val extensions: MutableSet<String> = mutableSetOf()) : ContextElement {
    override val key = CodecExtensions

    companion object : ContextKey<CodecExtensions> {
        override val isPerCommand = true
    }
}

internal class NodeLink : ContextElement {
    override val key = NodeLink

    val map = mutableMapOf<LibSpecNode, RSpecNode>()

    companion object : ContextKey<NodeLink> {
        override val isPerCommand = true
    }
}

internal class CodecWriteActions(val actions: MutableList<WriteAction> = mutableListOf()) : ContextElement {
    override val key = CodecWriteActions

    val helpersNeeded = mutableMapOf<String, KSClassDeclaration>()

    companion object : ContextKey<CodecWriteActions> {
        override val isPerCommand = true
    }
}

internal class KeyCollector(val keys: MutableList<LibSpecNode.ParameterNode> = mutableListOf()) : ContextElement {
    override val key = KeyCollector

    companion object : ContextKey<KeyCollector> {
        override val isPerCommand = true
    }
}
