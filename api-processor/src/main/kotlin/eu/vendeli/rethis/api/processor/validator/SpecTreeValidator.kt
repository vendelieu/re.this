package eu.vendeli.rethis.api.processor.validator

import com.google.devtools.ksp.KspExperimental
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
        val initErrorsSize = ctx.errors.size
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
        if (ctx.errors.size == initErrorsSize) kNode.validated = true
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
        val initErrorsSize = ctx.errors.size
        val tok = ctx.paramTree.findTokenByName(node.token)
            ?: return ctx.reportError("Missing pure‑token '${node.token}'")
        if (ctx.errors.size == initErrorsSize) tok.validated = true
        node.processed = true
    }

    override fun visitOneOf(node: SpecNode.OneOf, ctx: ValidationContext) {
        val initErrorsSize = ctx.errors.size
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

        val expect = node.children
            .map { it.token ?: it.normalizedName }
            .toSet()
        (expect - actual).forEach {
            ctx.reportError("Missing sub-entity '$it' in oneOf '${decl.simpleName.asString()}'")
        }
        (actual - expect).forEach {
            ctx.reportError("Unexpected sub-entity '$it' in oneOf '${decl.simpleName.asString()}'")
        }

        if (ctx.errors.size == initErrorsSize) kNode.validated = true
        node.children.forEach { if (!it.processed) it.accept(this, ctx) }
    }

    override fun visitBlock(node: SpecNode.Block, ctx: ValidationContext) {
        val kNode = node.token?.let {
            ctx.paramTree.findTokenByName(it)
        } ?: ctx.paramTree.findParameterByName(node.normalizedName)
        ?: return ctx.reportError("Missing block parameter '${node.normalizedName}'")
        val initErrorsSize = ctx.errors.size
        node.processed = true

        node.token?.let {
            if (ctx.paramTree.findTokenByName(it) == null) {
                ctx.reportError("Block token '$it' not found")
            }
        }

        if (ctx.errors.size == initErrorsSize) kNode.validated = true
        node.children.forEach { if (!it.processed) it.accept(this, ctx) }
    }

    fun collectOrders(specTree: List<SpecNode>): List<Pair<String, Float>> = specTree.flatMap { node ->
        listOf((node.token ?: node.normalizedName) to node.order) + collectOrders(node.children)
    }

    fun finalizeValidation(ctx: ValidationContext) {
        ctx.specTree.filter { !it.processed }.forEach {
            ctx.reportError("Not processed spec entry: '${it.name}'")
        }
    }
}
