package eu.vendeli.rethis.api.processor.validator

import com.squareup.kotlinpoet.ksp.toClassNameOrNull
import eu.vendeli.rethis.api.processor.type.LibSpecTree
import eu.vendeli.rethis.api.processor.type.SpecNode
import eu.vendeli.rethis.api.processor.type.SpecNodeVisitor
import eu.vendeli.rethis.api.processor.type.ValidationContext
import eu.vendeli.rethis.api.processor.utils.hasAnnotation
import eu.vendeli.rethis.api.processor.utils.parseIgnore
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.ValidityCheck

internal object SimpleValidator : SpecNodeVisitor {
    override fun visitSimple(node: SpecNode.Simple, ctx: ValidationContext) {
        val param = ctx.findParam(node.specName) ?: return
        val pType = param.symbol.type.resolve()
        val contextualOptional = checkContextualOptionality(param)

        if (node.optional && !contextualOptional) {
            ctx.reportError("${node.specName}: should be optional (marked with @RedisOptional and nullable or vararg)")
        }

        if (node.multiple && !param.symbol.isVararg && !pType.isCollection()) {
            ctx.reportError("${node.specName}: should be vararg or Collection")
        }

        val expected = node.type.specTypeNormalization()
        val actual = pType.toClassNameOrNull()?.simpleName?.lowercase()?.libTypeNormalization()
        if (expected != actual) {
            ctx.reportError("${node.specName}: expected type '$expected', found '$actual'")
        }

        if (node.token != null && !ctx.isTokenPresent(node.token)) {
            ctx.reportError("Token ${node.token} is not present")
            return
        }

        ctx.markProcessed(node.specName)
    }

    private tailrec fun checkContextualOptionality(node: LibSpecTree?): Boolean = when {
        node is LibSpecTree.ParameterNode && ( // if there's parameter in hierarchy
            ValidityCheck.OPTIONALITY in node.symbol.parseIgnore() || // or ignored check
            node.symbol.hasAnnotation<RedisOptional>() && (node.symbol.isVararg || node.symbol.type.resolve().isMarkedNullable)
            // or actually have optional marks
            )
            -> true

        node == null -> false
        else -> checkContextualOptionality(node.parent)
    }

    override fun visitPureToken(node: SpecNode.PureToken, ctx: ValidationContext) {}
    override fun visitOneOf(node: SpecNode.OneOf, ctx: ValidationContext) {}
    override fun visitBlock(node: SpecNode.Block, ctx: ValidationContext) {}
}
