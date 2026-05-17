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
            } ||
                // Sealed-class params dispatch on a variant whose constructor arity determines
                // the per-call wire-element count (e.g. CLIENT SETINFO's LibName(name) emits
                // <token> + <value>, two elements). Force a dynamic array header so the
                // precomputed `*N\r\n` doesn't undercount.
                ch.attr.filterIsInstance<EnrichedTreeAttr.Type>()
                    .any { it.type.declaration.isSealed() }
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

/**
 * A handle for a lambda binding (`it0`, `it1`, …) introduced by [CodeGenContext.inFor] or
 * [CodeGenContext.inLet]. The body receives a `Binding` and produces an [Access.Bound] via [ref];
 * the binding is registered as **used** the moment that access is rendered into the codec source
 * (see [Access.render]) — not when [ref] is called. So an [Access.Bound] that's built but never
 * actually emitted (e.g. a SIZE-phase counting body that ignores its iterator) keeps [isUsed]
 * false, and the surrounding scope can degrade the lambda parameter to `_`.
 *
 * [sourceField] is propagated into the access so [Access.qualify] can elide redundant
 * qualification when a leaf inside the scope shares the wrapper's field name.
 */
internal class Binding internal constructor(
    internal val name: String,
    internal val sourceField: String,
) {
    private var marked: Boolean = false
    val isUsed: Boolean get() = marked

    internal fun markUsed() {
        marked = true
    }

    /** Produce an access referring to this binding. Render-time consumption is what marks it used. */
    fun ref(): Access = Access.Bound(this)
}

/**
 * Pure code-emission helper: a thin façade over KotlinPoet's [CodeBlock.Builder] plus three
 * scope openers ([inFor], [inLet], [inWhen]) that follow a typed-access model.
 *
 * The previous implementation tracked block state itself (`pointer`, `blockStack`, `BlockType`,
 * `resolveAccess`, body-capture + regex). All of that is gone — scope context flows through
 * [Access] passed from caller to caller, and binding usage is declared by the body via
 * [Binding.ref] instead of detected after the fact.
 */
internal class CodeGenContext(
    private var builder: CodeBlock.Builder,
    private var nameCounter: Int = 0,
) : ContextElement {
    override val key = CodeGenContext

    // ---- KotlinPoet façade ----

    fun appendLine(line: String, vararg args: Any?) {
        builder.addStatement(line, *args)
    }

    fun beginControlFlow(controlFlow: String, vararg args: Any?) {
        builder.beginControlFlow(controlFlow, *args)
    }

    fun endControlFlow() {
        builder.endControlFlow()
    }

    /** Open a paired control-flow scope and run [block] inside it. */
    fun inScope(controlFlow: String, vararg args: Any?, block: () -> Unit) {
        builder.beginControlFlow(controlFlow, *args)
        block()
        builder.endControlFlow()
    }

    /** Emit a `<condition> -> { … }` branch inside an open `when`. */
    fun inWhenBranch(condition: String, block: () -> Unit) {
        inScope("$condition -> ") { block() }
    }

    /** Emit a literal `else -> {}` branch inside an open `when`. */
    fun appendElseEmpty() {
        builder.addStatement("else -> {}")
    }

    // ---- Scope openers ----

    /**
     * Emit `<target>.forEach { <param> -> … }`. The body runs against a scratch builder so we can
     * read [Binding.isUsed] before deciding the lambda parameter: if [block] never calls
     * `binding.ref()`, the parameter is `_`; otherwise it's the binding's name. The counter
     * advances in either case.
     *
     * The new binding inherits its `sourceField` from `target.tailField()` so subsequent leaf
     * accesses with the same field name resolve to the bare binding (see [Access.qualify]).
     */
    fun inFor(target: Access, block: (Binding) -> Unit) {
        val binding = Binding("it${nameCounter++}", sourceField = target.tailField())
        val body = captureScratch { block(binding) }
        val param = if (binding.isUsed) binding.name else "_"
        inScope("${target.render()}.forEach { $param ->") { builder.add(body) }
    }

    /** Emit `<target>?.let { <param> -> … }` with the same `_`-on-unused rule as [inFor]. */
    fun inLet(target: Access, block: (Binding) -> Unit) {
        val binding = Binding("it${nameCounter++}", sourceField = target.tailField())
        val body = captureScratch { block(binding) }
        val param = if (binding.isUsed) binding.name else "_"
        inScope("${target.render()}?.let { $param ->") { builder.add(body) }
    }

    /**
     * Emit `when (<subject>) { … }`. WHEN introduces no binding — children inherit [subject] as
     * their parent access (Kotlin smart-cast preserves the access path inside `is X -> { … }`).
     */
    fun inWhen(subject: Access, block: () -> Unit) {
        inScope("when (${subject.render()})") { block() }
    }

    /**
     * Render [block] into a fresh [CodeBlock] without writing to the live builder. The body's
     * emissions go to scratch; the caller can then prefix a header and replay the body via
     * `builder.add(...)`. Used by [inFor] and [inLet] to defer the header until [Binding.isUsed]
     * is known.
     */
    private inline fun captureScratch(block: () -> Unit): CodeBlock {
        val real = builder
        val scratch = CodeBlock.builder()
        builder = scratch
        try {
            block()
        } finally {
            builder = real
        }
        return scratch.build()
    }

    companion object : ContextKey<CodeGenContext> {
        override val isPerCommand = true
    }
}
