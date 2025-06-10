package eu.vendeli.rethis.api.processor.types

import eu.vendeli.rethis.api.processor.utils.normalizeParam

internal interface RSpecVisitor {
    fun visitSimple(node: RSpecNode.Simple)
    fun visitPureToken(node: RSpecNode.PureToken)
    fun visitOneOf(node: RSpecNode.OneOf)
    fun visitBlock(node: RSpecNode.Block)
}

internal sealed class RSpecNode {
    abstract val name: String
    open val path: List<Int> = emptyList()
    open val optional: Boolean = false
    open val multiple: Boolean = false
    abstract val token: String?
    open val parentNode: RSpecNode? = null
    open val children: List<RSpecNode> = emptyList()

    val normalizedName get() = name.normalizeParam()
    var processed: Boolean = false

    abstract fun accept(visitor: RSpecVisitor)

    data class Simple(
        val keyIdx: Int? = null,
        val type: String,
        override val name: String,
        override val path: List<Int>,
        override val optional: Boolean,
        override val multiple: Boolean,
        override val token: String? = null,
        override val parentNode: RSpecNode? = null,
    ) : RSpecNode() {
        override fun accept(visitor: RSpecVisitor) =
            visitor.visitSimple(this)
    }

    data class PureToken(
        override val name: String,
        override val path: List<Int>,
        override val token: String,
        override val parentNode: RSpecNode? = null,
    ) : RSpecNode() {
        override fun accept(visitor: RSpecVisitor) =
            visitor.visitPureToken(this)
    }

    data class OneOf(
        override val name: String,
        override val path: List<Int>,
        override val optional: Boolean,
        override val multiple: Boolean,
        override val token: String? = null,
        override val parentNode: RSpecNode? = null,
        override val children: List<RSpecNode>,
    ) : RSpecNode() {
        override fun accept(visitor: RSpecVisitor) =
            visitor.visitOneOf(this)
    }

    data class Block(
        override val name: String,
        override val path: List<Int>,
        override val token: String?,
        override val optional: Boolean,
        override val multiple: Boolean,
        override val parentNode: RSpecNode? = null,
        override val children: List<RSpecNode>,
    ) : RSpecNode() {
        override fun accept(visitor: RSpecVisitor) =
            visitor.visitBlock(this)
    }
}

internal class RSpecTreeBuilder(private val raw: List<CommandArgument>) {
    fun build() = raw.mapIndexed { idx, arg -> toNode(arg, listOf(idx)) }

    private fun toNode(
        arg: CommandArgument,
        path: List<Int>,
        parent: RSpecNode? = null,
    ): RSpecNode = when (arg.type) {
        "oneof" -> RSpecNode.OneOf(
            name = arg.name,
            path = path,
            optional = arg.optional,
            multiple = arg.multiple,
            token = arg.token,
            parentNode = parent,
            children = arg.arguments.mapIndexed { childIdx, childArg -> toNode(childArg, path + childIdx) },
        )

        "block" -> RSpecNode.Block(
            arg.name,
            path,
            arg.token,
            arg.optional,
            arg.multiple,
            parent,
            arg.arguments.mapIndexed { childIdx, childArg -> toNode(childArg, path + childIdx) },
        )

        "pure-token" -> RSpecNode.PureToken(name = arg.name, path = path, token = arg.token!!, parentNode = parent)

        else -> RSpecNode.Simple(
            keyIdx = arg.keySpecIndex,
            type = arg.type,
            name = arg.name,
            path = path,
            optional = arg.optional,
            multiple = arg.multiple,
            token = arg.token,
            parentNode = parent,
        )
    }
}
