package eu.vendeli.rethis.api.processor.validator

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import eu.vendeli.rethis.api.processor.type.SpecNode
import eu.vendeli.rethis.api.processor.type.SpecNodeVisitor
import eu.vendeli.rethis.api.processor.type.ValidationContext
import eu.vendeli.rethis.api.processor.utils.NameNormalizer

internal object OneOfValidator : SpecNodeVisitor {
    override fun visitOneOf(node: SpecNode.OneOf, ctx: ValidationContext) {
        ctx.markProcessed(node.name)
        val param = ctx.findParam(node.name) ?: return
        val allTokens = node.options.all { it is SpecNode.PureToken }
        if (allTokens) {
            PureTokenValidator.visitPureToken(
                SpecNode.PureToken(
                    node.name,
                    (node.options.first() as SpecNode.PureToken).token,
                ),
                ctx,
            )
            return
        }

        if (node.token != null && ctx.isTokenPresent(node.token)) {
            ctx.reportError("Oneof token ${node.token} is not present")
        }

        val decl = param.symbol.type.resolve().declaration as? KSClassDeclaration
        if (decl == null || Modifier.SEALED !in decl.modifiers) {
            ctx.reportError("${node.name}: should be sealed class")
        } else {
            val expected = node.options.map {
                NameNormalizer.normalizeClass((it as SpecNode.Simple).name)
            }.toSet()
            val actual = decl.declarations.filterIsInstance<KSClassDeclaration>()
                .filter { it.classKind == ClassKind.CLASS }
                .map { it.simpleName.asString() }.toSet()
            (expected - actual).forEach {
                ctx.reportError("${node.name}: missing subclass '$it'")
            }
            (actual - expected).forEach {
                ctx.reportError("${node.name}: unexpected subclass '$it'")
            }
        }
    }

    override fun visitSimple(node: SpecNode.Simple, ctx: ValidationContext) {}
    override fun visitPureToken(node: SpecNode.PureToken, ctx: ValidationContext) {}
    override fun visitBlock(node: SpecNode.Block, ctx: ValidationContext) {}
}
