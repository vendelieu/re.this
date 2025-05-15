package eu.vendeli.rethis.api.processor.validator

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.symbol.KSClassDeclaration
import eu.vendeli.rethis.api.processor.type.SpecNode
import eu.vendeli.rethis.api.processor.type.SpecNodeVisitor
import eu.vendeli.rethis.api.processor.type.ValidationContext
import eu.vendeli.rethis.api.processor.utils.getAnnotation
import eu.vendeli.rethis.api.processor.utils.hasAnnotation
import eu.vendeli.rethis.api.processor.utils.safeCast
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

internal object BlockValidator : SpecNodeVisitor {
    @OptIn(KspExperimental::class)
    override fun visitBlock(node: SpecNode.Block, ctx: ValidationContext) {
        val param = ctx.findParam(node.specName)
        ctx.markProcessed(node.specName)
        if (param == null) {
            node.children.forEach { it.accept(this, ctx) }
            return
        }

        val decl = param.symbol.type.resolve().declaration.safeCast<KSClassDeclaration>()
            ?: return ctx.reportError("${node.specName}: block not a class")

        if (node.token != null) {
            if (!ctx.isTokenPresent(node.token)) ctx.reportError("Token ${node.token} is not present")
            val ann = decl.getAnnotation<RedisOption.Name>()?.get("name") ?: decl.simpleName.asString()
            if (ann != node.token) ctx.reportError("${node.specName}: @Name mismatch '${node.token}'")
        }
    }

    override fun visitSimple(node: SpecNode.Simple, ctx: ValidationContext) {}
    override fun visitPureToken(node: SpecNode.PureToken, ctx: ValidationContext) {}
    override fun visitOneOf(node: SpecNode.OneOf, ctx: ValidationContext) {}
}
