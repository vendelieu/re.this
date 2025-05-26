package eu.vendeli.rethis.api.processor.type

import eu.vendeli.rethis.api.processor.utils.normalizeParam

internal interface SpecNodeVisitor {
    fun visitSimple(node: SpecNode.Simple, ctx: ValidationContext)
    fun visitPureToken(node: SpecNode.PureToken, ctx: ValidationContext)
    fun visitOneOf(node: SpecNode.OneOf, ctx: ValidationContext)
    fun visitBlock(node: SpecNode.Block, ctx: ValidationContext)
}


internal sealed class SpecNode {
    abstract val name: String
    open val optional: Boolean = false
    open val multiple: Boolean = false
    abstract val token: String?
    open val parentNode: SpecNode? = null
    open val children: List<SpecNode> = emptyList()
    abstract val order: Float

    val normalizedName get() = name.normalizeParam()
    var processed: Boolean = false

    abstract fun accept(visitor: SpecNodeVisitor, ctx: ValidationContext)

    data class Simple(
        val type: String,
        override val name: String,
        override val optional: Boolean,
        override val multiple: Boolean,
        override val token: String? = null,
        override val parentNode: SpecNode? = null,
        override val order: Float,
        val idx: Int? = null,
    ) : SpecNode() {
        override fun accept(visitor: SpecNodeVisitor, ctx: ValidationContext) =
            visitor.visitSimple(this, ctx)
    }

    data class PureToken(
        override val name: String,
        override val token: String,
        override val parentNode: SpecNode? = null,
        override val order: Float,
    ) : SpecNode() {
        override fun accept(visitor: SpecNodeVisitor, ctx: ValidationContext) =
            visitor.visitPureToken(this, ctx)
    }

    data class OneOf(
        override val name: String,
        override val children: List<SpecNode>,
        override val optional: Boolean,
        override val multiple: Boolean,
        override val token: String? = null,
        override val parentNode: SpecNode? = null,
        override val order: Float,
    ) : SpecNode() {
        override fun accept(visitor: SpecNodeVisitor, ctx: ValidationContext) =
            visitor.visitOneOf(this, ctx)
    }

    data class Block(
        override val name: String,
        override val token: String?,
        override val children: List<SpecNode>,
        override val optional: Boolean,
        override val multiple: Boolean,
        override val parentNode: SpecNode? = null,
        override val order: Float,
    ) : SpecNode() {
        override fun accept(visitor: SpecNodeVisitor, ctx: ValidationContext) =
            visitor.visitBlock(this, ctx)
    }
}

internal fun SpecNode.collectAllChildren(): List<SpecNode> {
    val allChildren = mutableListOf<SpecNode>()
    fun traverse(node: SpecNode) {
        node.children.forEach { child ->
            allChildren.add(child)
            traverse(child)
        }
    }
    traverse(this)
    return allChildren
}

internal class SpecTreeBuilder(private val raw: List<CommandArgument>) {
    fun build(): List<SpecNode> =
        raw.mapIndexed { idx, arg -> toNode(arg, idx.toFloat()) }

    private fun toNode(arg: CommandArgument, order: Float, parent: SpecNode? = null): SpecNode =
        when (arg.type) {
            "oneof" -> SpecNode.OneOf(
                name = arg.name,
                optional = arg.optional,
                multiple = arg.multiple,
                token = arg.token,
                parentNode = parent,
                order = order,
                children = arg.arguments.mapIndexed { i, c -> toNode(c, order.toSubOrder(i), parent) },
            )

            "block" -> SpecNode.Block(
                name = arg.name,
                token = arg.token,
                optional = arg.optional,
                multiple = arg.multiple,
                parentNode = parent,
                order = order,
                children = arg.arguments.mapIndexed { i, c -> toNode(c, order.toSubOrder(i), parent) },
            )

            "pure-token" -> SpecNode.PureToken(
                name = arg.name,
                token = arg.token!!,
                parentNode = parent,
                order = order,
            )

            else -> SpecNode.Simple(
                type = arg.type,
                name = arg.name,
                optional = arg.optional,
                multiple = arg.multiple,
                token = arg.token,
                parentNode = parent,
                order = order,
                idx = arg.keySpecIndex,
            )
        }

    private fun Float.toSubOrder(modifier: Int): Float = this + (modifier + 1) / 10F
}
