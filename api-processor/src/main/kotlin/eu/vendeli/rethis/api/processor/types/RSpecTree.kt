package eu.vendeli.rethis.api.processor.types

import eu.vendeli.rethis.api.processor.utils.normalizeParam

internal sealed class RSpecNode {
    abstract val name: String
    abstract val arg: CommandArgument
    open val path: List<Int> = emptyList()
    open val parentNode: RSpecNode? = null
    open val children: MutableList<RSpecNode> = mutableListOf()

    val normalizedName get() = name.normalizeParam()

    data class Simple(
        val type: String,
        override val name: String,
        override val arg: CommandArgument,
        override val path: List<Int>,
        override val parentNode: RSpecNode? = null,
    ) : RSpecNode()

    data class PureToken(
        override val name: String,
        override val arg: CommandArgument,
        override val path: List<Int>,
        override val parentNode: RSpecNode? = null,
    ) : RSpecNode()

    data class OneOf(
        override val name: String,
        override val arg: CommandArgument,
        override val path: List<Int>,
        override val parentNode: RSpecNode? = null,
    ) : RSpecNode()

    data class Block(
        override val name: String,
        override val arg: CommandArgument,
        override val path: List<Int>,
        override val parentNode: RSpecNode? = null,
    ) : RSpecNode()

    fun addChildren(children: List<RSpecNode>): RSpecNode {
        this.children.addAll(children)
        return this
    }
}

internal object RSpecTreeBuilder {
    fun build(raw: List<CommandArgument>) = raw.mapIndexed { idx, arg -> toNode(arg, listOf(idx)) }

    private fun toNode(
        arg: CommandArgument,
        path: List<Int>,
        parent: RSpecNode? = null,
    ): RSpecNode = when (arg.type) {
        "oneof" -> RSpecNode.OneOf(
            name = arg.name,
            arg = arg,
            path = path,
            parentNode = parent,
        ).apply {
            addChildren(
                arg.arguments.mapIndexed { childIdx, childArg ->
                    toNode(childArg, path + childIdx, this)
                },
            )
        }

        "block" -> RSpecNode.Block(
            name = arg.name,
            arg = arg,
            path = path,
            parentNode = parent,
        ).apply {
            addChildren(
                arg.arguments.mapIndexed { childIdx, childArg ->
                    toNode(childArg, path + childIdx, this)
                },
            )
        }

        "pure-token" -> RSpecNode.PureToken(
            name = arg.name,
            arg = arg,
            path = path,
            parentNode = parent,
        )

        else -> RSpecNode.Simple(
            type = arg.type,
            name = arg.name,
            arg = arg,
            path = path,
            parentNode = parent,
        )
    }
}
