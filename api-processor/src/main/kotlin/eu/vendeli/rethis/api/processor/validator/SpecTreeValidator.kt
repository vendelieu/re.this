package eu.vendeli.rethis.api.processor.validator

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import eu.vendeli.rethis.api.processor.type.*
import eu.vendeli.rethis.api.processor.type.SpecNode.PureToken
import eu.vendeli.rethis.api.processor.utils.*
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.ValidityCheck

internal object SpecTreeValidator : SpecNodeVisitor {
    // 1) Structural & semantic checks
    @OptIn(KspExperimental::class)
    override fun visitSimple(node: SpecNode.Simple, ctx: ValidationContext) {
        if (node.processed) return
        // Custom‑codec bypass
        if (ctx.paramTree.findParameterByName(node.normalizedName)?.symbol
                ?.hasAnnotation<RedisMeta.CustomCodec>() == true
        ) {
            node.processed = true
            return
        }

        val kNode = ctx.paramTree.findParameterByName(node.normalizedName)
            ?: return ctx.reportError("Missing lib parameter for spec '${node.normalizedName}'")
        node.processed = true

        // Token presence
        node.token?.let {
            if (ctx.paramTree.findTokenByName(it) == null) {
                ctx.reportError("Token '$it' not found for '${node.normalizedName}'")
            }
        }

        // Type check
        val expectedK = node.type.lowercase().specTypeNormalization()
        val actualK = kNode.symbol.type
            .resolve()
            .declaration
            .simpleName
            .asString().lowercase().libTypeNormalization()
        if (!actualK.equals(expectedK, true)) {
            ctx.reportError(
                "Type mismatch for '${node.normalizedName}': expected $expectedK, got $actualK",
            )
        }

        // Optionality
        val t = kNode.symbol.type.resolve()
        val contextualOptional = checkContextualOptionality(kNode)

        if (node.optional && !contextualOptional) {
            ctx.reportError("'${node.normalizedName}' must be optional")
        }

        // Multiple
        if (node.multiple && !kNode.symbol.isVararg && !t.isCollection()) {
            ctx.reportError("'${node.normalizedName}' must be repeatable")
        }
    }

    private tailrec fun checkContextualOptionality(node: LibSpecTree?): Boolean = when {
        node is LibSpecTree.ParameterNode && ( // if there's parameter in hierarchy
            ValidityCheck.OPTIONALITY in node.symbol.parseIgnore() || // or ignored check
                node.symbol.hasAnnotation<RedisOptional>() && // or actually have optional marks
                (node.symbol.isVararg || node.symbol.type.resolve().let { it.isCollection() || it.isMarkedNullable })
            )
            -> true

        node == null -> false
        else -> checkContextualOptionality(node.parent)
    }

    override fun visitPureToken(node: PureToken, ctx: ValidationContext) {
        val tok = ctx.paramTree.findTokenByName(node.token)
            ?: return ctx.reportError("Missing pure‑token '${node.token}'")
        node.processed = true
    }

    override fun visitOneOf(node: SpecNode.OneOf, ctx: ValidationContext) {
        val kNode = ctx.paramTree.findParameterByName(node.normalizedName)
            ?: return ctx.reportError("Missing oneof parameter '${node.normalizedName}'")
        node.processed = true

        // Must be sealed
        val decl = kNode.symbol.type.resolve().declaration.safeCast<KSClassDeclaration>()
            ?: return ctx.reportError("'${node.normalizedName}' must be sealed")
        if (!decl.modifiers.contains(Modifier.SEALED) && !decl.isEnum()) {
            ctx.reportError("'${node.normalizedName}' must be a sealed class")
        }

        val isEnum = decl.isEnum()
        // 1:1 subclass match
        val actual = if (isEnum) {
            decl.declarations.filterIsInstance<KSClassDeclaration>().filter {
                it.classKind == ClassKind.ENUM_ENTRY
            }.map {
                it.effectiveName()
            }.toSet()
        } else {
            decl.getSealedSubclasses()
                .map { it.effectiveName() }
                .toSet()
        }

        val expect = node.options
            .map { it.token ?: it.name }
            .toSet()
        (expect - actual).forEach {
            ctx.reportError("Missing sub-entity '$it' in oneOf '${decl.simpleName.asString()}'")
        }
        (actual - expect).forEach {
            ctx.reportError("Unexpected sub-entity '$it' in oneOf '${decl.simpleName.asString()}'")
        }

        node.options.forEach { if (!it.processed) it.accept(this, ctx) }
    }

    override fun visitBlock(node: SpecNode.Block, ctx: ValidationContext) {
        val kNode = ctx.paramTree.findParameterByName(node.normalizedName)
            ?: return ctx.reportError("Missing block parameter '${node.normalizedName}'")
        node.processed = true

        node.token?.let {
            if (ctx.paramTree.findTokenByName(it) == null) {
                ctx.reportError("Block token '$it' not found")
            }
        }

        node.children.forEach { if (!it.processed) it.accept(this, ctx) }
    }

    // 2) Order validation
    fun validateOrder(ctx: ValidationContext) {
        val top = ctx.fullSpec.commands[ctx.currentCmd]?.arguments ?: return
        recurseOrder(top, ctx, path = listOf(ctx.currentCmd))
    }

    @OptIn(KspExperimental::class)
    private fun recurseOrder(
        args: List<CommandArgument>,
        ctx: ValidationContext,
        path: List<String>,
    ) {
        // require explicit @OrderPriority
        val siblings = args.mapIndexed { idx, arg ->
            val display = (path + arg.specName).joinToString(" → ")
            val kNode = arg.token
                ?.let { ctx.paramTree.findTokenByName(it) }
                ?: ctx.paramTree.findParameterByName(arg.normalizedName)

            val prio = kNode?.symbol
                ?.getAnnotationsByType(RedisMeta.OrderPriority::class)
                ?.firstOrNull()?.priority

            display to (prio ?: Int.MIN_VALUE)
        }
        if (siblings.any { it.second == Int.MIN_VALUE }) return

        // strictly increasing
        siblings.zipWithNext().forEach { (l, r) ->
            if (r.second <= l.second) {
                ctx.reportError(
                    "Order violation: '${r.first}'(=${r.second}) must come after '${l.first}'(=${l.second})",
                )
            }
        }

        // contiguous
        val prios = siblings.map { it.second }.toSet()
        val minP = prios.minOrNull()!!
        val maxP = prios.maxOrNull()!!
        if (prios != (minP..maxP).toSet()) {
            ctx.reportError(
                "Non‑contiguous @OrderPriority at '${path.joinToString(" → ")}': $prios vs ${minP..maxP}",
            )
        }

        // recurse
        args.forEach {
            if (it.arguments.isNotEmpty()) {
                recurseOrder(it.arguments, ctx, path + it.specName)
            }
        }
    }

    fun finalizeValidation(ctx: ValidationContext) {
        validateOrder(ctx)
        ctx.specTree.filter { !it.processed }
            .forEach { ctx.reportError("Not processed spec entry: '${it.name}'") }
    }
}
