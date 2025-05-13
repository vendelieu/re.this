package eu.vendeli.rethis.api.processor.validator

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAnnotationPresent
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
        val param = ctx.findParam(node.name)
        ctx.markProcessed(node.name)
        if (param == null) {
            node.children.forEach { it.accept(this, ctx) }
            return
        }

        val decl = param.type.resolve().declaration.safeCast<KSClassDeclaration>()
            ?: return ctx.reportError("${node.name}: block not a class")

        if (node.token != null) {
            val ann = decl.getAnnotation<RedisOption.Name>()?.get("name") ?: decl.simpleName.asString()
            if (ann != node.token) ctx.reportError("${node.name}: @Name mismatch '${node.token}'")
        }
//        else if (!decl.hasAnnotation<RedisOption.SkipName>()) { // todo check why false pos
//            ctx.reportError("${node.name}: missing @SkipName")
//        } // todo turn back when fixed bug in ksp
    }

    override fun visitSimple(node: SpecNode.Simple, ctx: ValidationContext) {}
    override fun visitPureToken(node: SpecNode.PureToken, ctx: ValidationContext) {}
    override fun visitOneOf(node: SpecNode.OneOf, ctx: ValidationContext) {}
}
