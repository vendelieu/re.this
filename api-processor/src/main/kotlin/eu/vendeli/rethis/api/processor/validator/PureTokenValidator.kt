package eu.vendeli.rethis.api.processor.validator

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import eu.vendeli.rethis.api.processor.type.SpecNode
import eu.vendeli.rethis.api.processor.type.SpecNodeVisitor
import eu.vendeli.rethis.api.processor.type.ValidationContext
import eu.vendeli.rethis.api.processor.utils.getAnnotation
import eu.vendeli.rethis.api.processor.utils.safeCast
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

internal object PureTokenValidator : SpecNodeVisitor {
    override fun visitPureToken(node: SpecNode.PureToken, ctx: ValidationContext) {
        val param = ctx.findParam(node.name) ?: return
        val decl = param.type.resolve().declaration.safeCast<KSClassDeclaration>()
        val isOneOfToken = node.parentNode is SpecNode.OneOf
        ctx.markProcessed(node.name)

        when {
            isOneOfToken && decl?.classKind != ClassKind.ENUM_CLASS -> {
                ctx.reportError("${node.name}: should be enum")
            }

            !isOneOfToken -> {
                if (node.parentNode != null &&
                    decl?.classKind != ClassKind.OBJECT
                ) ctx.reportError("${node.name}: should be data object")
            }

            else -> {
                val entries = decl!!.declarations.filterIsInstance<KSClassDeclaration>()
                    .filter { it.classKind == ClassKind.ENUM_ENTRY }
                    .map {
                        it.getAnnotation<RedisOption.Name>()?.get("name") ?: it.simpleName.asString()
                    }.toSet()
                if (node.token !in entries)
                    ctx.reportError("${node.name}: enum entries $entries, absent {${node.token}}")
            }
        }
    }

    override fun visitSimple(node: SpecNode.Simple, ctx: ValidationContext) {}
    override fun visitOneOf(node: SpecNode.OneOf, ctx: ValidationContext) {}
    override fun visitBlock(node: SpecNode.Block, ctx: ValidationContext) {}
}
