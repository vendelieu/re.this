package eu.vendeli.rethis.api.processor.validator

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Modifier
import eu.vendeli.rethis.api.processor.context.KeyCollector
import eu.vendeli.rethis.api.processor.core.RedisCommandProcessor.Companion.context
import eu.vendeli.rethis.api.processor.types.*
import eu.vendeli.rethis.api.processor.utils.*
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.ValidityCheck

internal object SpecTreeValidator : RSpecVisitor {
    @OptIn(KspExperimental::class)
    override fun visitSimple(node: RSpecNode.Simple) {
        if (node.processed) return

        // 1) Find the corresponding LibSpecNode.ParameterNode, if any
        val libNode: LibSpecNode.ParameterNode? = context.libSpecTree.findParameterByName(node.normalizedName)

        // 2) Always record path → LibSpecNode mapping for generator
        if (libNode != null) {
            context.nodeLink[libNode] = node
            if (node.keyIdx != null) context[KeyCollector]!!.keys.add(libNode)
        }

        // 3) If there's a custom encoder on that parameter, skip further checks
        if (libNode?.symbol?.hasCustomEncoder() == true) {
            node.processed = true
            return
        }

        // 4) Token‐presence check: if JSON specified a token, it must exist in the spec
        node.token?.let { tok ->
            if (libNode != null && context.libSpecTree.findTokenByName(tok) == null) {
                context.currentCommand.reportError("Token '$tok' not found for '${node.normalizedName}'")
            }
        }

        // 5) If no matching parameter, nothing more to validate here
        if (libNode == null) return

        // 6) Semantic checks: type, optionality, repeatability, annotation
        val paramKS = libNode.symbol.safeCast<KSValueParameter>() ?: return
        val ignores = paramKS.parseIgnore()

        // a) Type‐check
        val expectedType = node.type.lowercase().specTypeNormalization()
        val actualType = paramKS.type
            .collectionAwareType()
            .declaration
            .simpleName
            .asString()
            .lowercase()
            .libTypeNormalization()
        if (!ignores.contains(ValidityCheck.TYPE) && !actualType.equals(expectedType, ignoreCase = true)) {
            context.currentCommand.reportError(
                "Type mismatch for '${node.normalizedName}': expected $expectedType, got $actualType",
            )
        }

        // b) Optionality
        val paramKSType = paramKS.type.resolve()
        val isContextuallyOptional = checkContextualOptionality(libNode)
        if (!ignores.contains(ValidityCheck.OPTIONALITY) && node.optional && !isContextuallyOptional) {
            context.currentCommand.reportError("'${node.normalizedName}' must be optional")
        }

        // c) Repeatability (multiple)
        if (!ignores.contains(ValidityCheck.REPEATABILITY)
            && node.multiple
            && !paramKS.isVararg
            && !paramKSType.isCollection()
        ) {
            context.currentCommand.reportError("'${node.normalizedName}' must be repeatable\n${paramKS.parent?.parent}")
        }

        node.processed = true
    }

    private tailrec fun checkContextualOptionality(node: LibSpecNode?): Boolean = when {
        node is LibSpecNode.ParameterNode && (
            ValidityCheck.OPTIONALITY in node.symbol.parseIgnore() ||
                node.symbol.hasAnnotation<RedisOptional>() &&
                (node.symbol.isVararg || node.symbol.type.resolve().let { it.isCollection() || it.isMarkedNullable })
            ) -> true

        node == null -> false
        else -> checkContextualOptionality(node.parent)
    }

    override fun visitPureToken(node: RSpecNode.PureToken) {
        if (context.libSpecTree.findTokenByName(node.token) != null) {
            node.processed = true
        } else if (getByCommandsByName(node.normalizedName)?.size?.let { it < 2 } == true) {
            context.currentCommand.reportError("Token '${node.token}' not found")
        }
    }

    override fun visitOneOf(node: RSpecNode.OneOf) {
        // 1) Validate children first
        node.children.forEach { child ->
            if (!child.processed) child.accept(SpecTreeValidator)
        }

        // 2) Token‐presence check for the OneOf wrapper
        node.token?.let { tok ->
            if (context.libSpecTree.findTokenByName(tok) == null) {
                context.currentCommand.reportError("OneOf token '$tok' not found")
            } else {
                node.processed = true
            }
        }

        // 3) If all children passed, mark this node processed
        if (node.children.all { it.processed }) {
            node.processed = true
        }

        // 4) Find corresponding parameter by name
        val libNode: LibSpecNode = context.libSpecTree.findParameterByName(node.normalizedName) ?: return

        // 5) Record path → LibSpecNode mapping
        context.nodeLink[libNode] = node

        // 6) The Kotlin type must be sealed or enum
        val decl =
            libNode.symbol.safeCast<KSValueParameter>()?.type?.resolve()?.declaration.safeCast<KSClassDeclaration>()
                ?: return context.currentCommand.reportError("'${node.normalizedName}' must be a sealed/enum class!")
        if (!decl.modifiers.contains(Modifier.SEALED) && !decl.isEnum()) {
            context.currentCommand.reportError("'${node.normalizedName}' must be a sealed/enum class")
        }

        node.processed = true
    }

    override fun visitBlock(node: RSpecNode.Block) {
        // 1) Validate children first
        node.children.forEach { child ->
            if (!child.processed) child.accept(SpecTreeValidator)
        }

        // 2) Token‐presence check for the block wrapper
        node.token?.let { tok ->
            if (context.libSpecTree.findTokenByName(tok) == null) {
                context.currentCommand.reportError("Block token '$tok' not found")
            }
        }

        node.processed = true

        // 3) Find corresponding parameter by name
        val libNode: LibSpecNode = context.libSpecTree.findParameterByName(node.normalizedName) ?: return

        // 4) Record path → LibSpecNode mapping
        context.nodeLink[libNode] = node

        // 5) Repeatability check
        if (node.multiple && libNode.symbol.safeCast<KSValueParameter>()?.isVararg == false) {
            context.currentCommand.reportError("'${node.normalizedName}' must be repeatable")
        }
        // 6) Optionality check
        if (node.optional && !checkContextualOptionality(libNode)) {
            context.currentCommand.reportError("Block '${node.normalizedName}' must be optional")
        }
    }
}
