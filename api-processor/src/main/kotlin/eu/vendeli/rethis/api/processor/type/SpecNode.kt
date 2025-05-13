package eu.vendeli.rethis.api.processor.type

internal sealed class SpecNode(
    val name: String,
    val optional: Boolean,
    val multiple: Boolean,
    open val parentNode: SpecNode? = null,
) {
    abstract fun accept(visitor: SpecNodeVisitor, ctx: ValidationContext)

    data class Simple(
        val type: String,
        val specName: String,
        val isOptional: Boolean,
        val isMultiple: Boolean,
        override val parentNode: SpecNode? = null,
    ) : SpecNode(specName, isOptional, isMultiple) {
        override fun accept(visitor: SpecNodeVisitor, ctx: ValidationContext) =
            visitor.visitSimple(this, ctx)
    }

    data class PureToken(
        val specName: String,
        val token: String,
        override val parentNode: SpecNode? = null,
    ) : SpecNode(specName, optional = false, multiple = false) {
        override fun accept(visitor: SpecNodeVisitor, ctx: ValidationContext) =
            visitor.visitPureToken(this, ctx)
    }

    data class OneOf(
        val specName: String,
        val options: List<SpecNode>,
        val isOptional: Boolean,
        val isMultiple: Boolean,
        override val parentNode: SpecNode? = null,
    ) : SpecNode(specName, isOptional, isMultiple) {
        override fun accept(visitor: SpecNodeVisitor, ctx: ValidationContext) =
            visitor.visitOneOf(this, ctx)
    }

    data class Block(
        val specName: String,
        val token: String?,
        val children: List<SpecNode>,
        val isOptional: Boolean,
        val isMultiple: Boolean,
        override val parentNode: SpecNode? = null,
    ) : SpecNode(specName, isOptional, isMultiple) {
        override fun accept(visitor: SpecNodeVisitor, ctx: ValidationContext) =
            visitor.visitBlock(this, ctx)
    }
}
