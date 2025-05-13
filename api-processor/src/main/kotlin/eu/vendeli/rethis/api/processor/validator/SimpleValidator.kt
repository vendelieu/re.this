package eu.vendeli.rethis.api.processor.validator

import com.squareup.kotlinpoet.ksp.toClassName
import eu.vendeli.rethis.api.processor.type.SpecNode
import eu.vendeli.rethis.api.processor.type.SpecNodeVisitor
import eu.vendeli.rethis.api.processor.type.ValidationContext
import eu.vendeli.rethis.api.processor.utils.hasAnnotation
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional

internal object SimpleValidator : SpecNodeVisitor {
    override fun visitSimple(node: SpecNode.Simple, ctx: ValidationContext) {
        val param = ctx.findParam(node.name) ?: return
        val pType = param.type.resolve()

        if (node.optional && !param.isVararg && !pType.isMarkedNullable)
            ctx.reportError("${node.name}: should be nullable")

        if (node.optional && !param.hasAnnotation<RedisOptional>())
            ctx.reportError("${node.name}: should be annotated with @RedisOptional")

        if (node.multiple && !param.isVararg && !pType.isCollection())
            ctx.reportError("${node.name}: should be vararg or Collection")

        val expected = node.type.specTypeNormalization()
        val actual = pType.toClassName().simpleName.lowercase().libTypeNormalization()
        if (expected != actual)
            ctx.reportError("${node.name}: expected type '$expected', found '$actual'")
        ctx.markProcessed(node.name)
    }

    override fun visitPureToken(node: SpecNode.PureToken, ctx: ValidationContext) {}
    override fun visitOneOf(node: SpecNode.OneOf, ctx: ValidationContext) {}
    override fun visitBlock(node: SpecNode.Block, ctx: ValidationContext) {}
}
