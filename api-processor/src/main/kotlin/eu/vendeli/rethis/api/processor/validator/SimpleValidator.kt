package eu.vendeli.rethis.api.processor.validator

import com.squareup.kotlinpoet.ksp.toClassName
import eu.vendeli.rethis.api.processor.type.LibSpecNode
import eu.vendeli.rethis.api.processor.type.SpecNode
import eu.vendeli.rethis.api.processor.type.SpecNodeVisitor
import eu.vendeli.rethis.api.processor.type.ValidationContext
import eu.vendeli.rethis.api.processor.utils.hasAnnotation
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional

internal object SimpleValidator : SpecNodeVisitor {
    override fun visitSimple(node: SpecNode.Simple, ctx: ValidationContext) {
        val param = ctx.findParam(node.specName) ?: return
        val pType = param.symbol.type.resolve()
        val contextualOptional = checkContextualOptionality(param)

        if (node.optional && !contextualOptional)
            ctx.reportError("${node.specName}: should be optional (marked with @RedisOptional and nullable or vararg)")

        if (node.multiple && !param.symbol.isVararg && !pType.isCollection())
            ctx.reportError("${node.specName}: should be vararg or Collection")

        if (node.token != null && !ctx.isTokenPresent(node.token)) {
            ctx.reportError("Token ${node.token} is not present")
        }

        val expected = node.type.specTypeNormalization()
        val actual = pType.toClassName().simpleName.lowercase().libTypeNormalization()
        if (expected != actual)
            ctx.reportError("${node.specName}: expected type '$expected', found '$actual'")
        ctx.markProcessed(node.specName)
    }

    private tailrec fun checkContextualOptionality(node: LibSpecNode?): Boolean = when {
        node is LibSpecNode.ParameterNode &&
            node.symbol.hasAnnotation<RedisOptional>() && (node.symbol.isVararg || node.symbol.type.resolve().isMarkedNullable)
            -> true

        node == null -> false
        else -> checkContextualOptionality(node.parent)
    }

    override fun visitPureToken(node: SpecNode.PureToken, ctx: ValidationContext) {}
    override fun visitOneOf(node: SpecNode.OneOf, ctx: ValidationContext) {}
    override fun visitBlock(node: SpecNode.Block, ctx: ValidationContext) {}
}
