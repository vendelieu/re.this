package eu.vendeli.rethis.api.processor.validator

import eu.vendeli.rethis.api.processor.type.SpecNode
import eu.vendeli.rethis.api.processor.type.SpecNodeVisitor
import eu.vendeli.rethis.api.processor.type.ValidationContext

internal object PureTokenValidator : SpecNodeVisitor {
    override fun visitPureToken(node: SpecNode.PureToken, ctx: ValidationContext) {
        if (ctx.isTokenPresent(node.token)) ctx.markProcessed(node.specName)
    }

    override fun visitSimple(node: SpecNode.Simple, ctx: ValidationContext) {}
    override fun visitOneOf(node: SpecNode.OneOf, ctx: ValidationContext) {}
    override fun visitBlock(node: SpecNode.Block, ctx: ValidationContext) {}
}
