package eu.vendeli.rethis.api.processor.validator

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import eu.vendeli.rethis.api.processor.types.*
import eu.vendeli.rethis.api.processor.types.SpecNode.PureToken
import eu.vendeli.rethis.api.processor.utils.*
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.ValidityCheck

internal object SpecTreeValidator : SpecNodeVisitor {
    // 1) Structural & semantic checks
    @OptIn(KspExperimental::class)
    override fun visitSimple(node: SpecNode.Simple, ctx: ValidationContext) {
        if (node.processed) return
        // Custom‑codec bypass
        val param = ctx.paramTree.findParameterByName(node.normalizedName)

        if (param?.symbol?.hasCustomEncoder() == true) {
            node.processed = true
            return
        }
        // Token presence
        node.token?.let {
            if (param != null && ctx.paramTree.findTokenByName(it) == null) {
                ctx.reportError("Token '$it' not found for '${node.normalizedName}'")
            }
        }

        if (param == null) return
        val ignores = param.symbol.parseIgnore()

        // Type check
        val expectedK = node.type.lowercase().specTypeNormalization()
        val actualK = param.symbol.type
            .resolve().let {
                if (it.isCollection()) it.arguments.first().type?.resolve()
                else it
            }
            ?.declaration
            ?.simpleName
            ?.asString()?.lowercase()?.libTypeNormalization()
        if (!ignores.contains(ValidityCheck.TYPE) && !actualK.equals(expectedK, true)) {
            ctx.reportError(
                "Type mismatch for '${node.normalizedName}': expected $expectedK, got $actualK",
            )
        }

        // Optionality
        val t = param.symbol.type.resolve()
        val contextualOptional = checkContextualOptionality(param)

        if (!ignores.contains(ValidityCheck.OPTIONALITY) && node.optional && !contextualOptional) {
            ctx.reportError("'${node.normalizedName}' must be optional")
        }

        // Multiple
        if (!ignores.contains(ValidityCheck.REPEATABILITY) && node.multiple && !param.symbol.isVararg && !t.isCollection()) {
            ctx.reportError("'${node.normalizedName}' must be repeatable\n${param.symbol.parent?.parent}")
        }

        // Key
        if (node.idx != null && !param.symbol.hasAnnotation<RedisKey>()) {
            ctx.reportError("Parameter `${node.normalizedName}` is not annotated with @RedisKey")
        }

        node.processed = true
    }

    private tailrec fun checkContextualOptionality(node: LibSpecTree?): Boolean = when {
        node is LibSpecTree.ParameterNode && ( // if there's parameter in hierarchy
            ValidityCheck.OPTIONALITY in node.symbol.parseIgnore() || // or ignored check
                node.symbol.hasAnnotation<RedisOptional>() && // or actually have optional marks
                (node.symbol.isVararg || node.symbol.type.resolve().let { it.isCollection() || it.isMarkedNullable })
            ) -> true

        node == null -> false
        else -> checkContextualOptionality(node.parent)
    }

    override fun visitPureToken(node: PureToken, ctx: ValidationContext) {
        if (ctx.paramTree.findTokenByName(node.token) != null) {
            node.processed = true
        } else if (!ctx.isMultiSpec) ctx.reportError(
            "Token '${node.token}' not found",
        )
    }

    override fun visitOneOf(
        node: SpecNode.OneOf,
        ctx: ValidationContext,
    ) {
        node.children.forEach { if (!it.processed) it.accept(SpecTreeValidator, ctx) }
        node.token?.let {
            if (ctx.paramTree.findTokenByName(it) == null) {
                ctx.reportError("OneOf token '$it' not found")
            } else {
                node.processed = true
            }
        }

        if (node.children.all { it.processed }) {
            node.processed = true
        }

        val param = ctx.paramTree.findParameterByName(node.normalizedName) ?: return
        // Must be sealed
        val decl = param.symbol.type.resolve().declaration.safeCast<KSClassDeclaration>()
            ?: return ctx.reportError("'${node.normalizedName}' must be a sealed/enum class!")
        if (!decl.modifiers.contains(Modifier.SEALED) && !decl.isEnum()) {
            ctx.reportError("'${node.normalizedName}' must be a sealed/enum class")
        }
        node.processed = true
    }

    override fun visitBlock(node: SpecNode.Block, ctx: ValidationContext) {
        node.children.forEach { if (!it.processed) it.accept(SpecTreeValidator, ctx) }
        node.token?.let {
            if (ctx.paramTree.findTokenByName(it) == null) {
                ctx.reportError("Block token '$it' not found")
            }
        }
        node.processed = true

        val param = ctx.paramTree.findParameterByName(node.normalizedName) ?: return
        if (node.multiple && !param.symbol.isVararg) {
            ctx.reportError("'${node.normalizedName}' must be repeatable")
        }
        if (node.optional && !checkContextualOptionality(param)) {
            ctx.reportError("Block '${node.normalizedName}' must be optional")
        }
    }
}
