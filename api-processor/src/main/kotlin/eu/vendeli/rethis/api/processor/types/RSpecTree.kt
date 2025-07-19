package eu.vendeli.rethis.api.processor.types

import eu.vendeli.rethis.api.processor.utils.normalizeParam

internal sealed class RSpecNode {
    abstract val name: String
    abstract val arg: CommandArgument
    open val path: List<Int> = emptyList()
    open val parentNode: RSpecNode? = null
    open val children: List<RSpecNode> = emptyList()

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
        override val children: List<RSpecNode>,
    ) : RSpecNode()

    data class Block(
        override val name: String,
        override val arg: CommandArgument,
        override val path: List<Int>,
        override val parentNode: RSpecNode? = null,
        override val children: List<RSpecNode>,
    ) : RSpecNode()
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
            arg = arg,
            path = path,
            parentNode = parent,
            children = arg.arguments.mapIndexed { childIdx, childArg -> toNode(childArg, path + childIdx) },
        )

        "block" -> RSpecNode.Block(
            name = arg.name,
            arg = arg,
            path = path,
            parentNode = parent,
            children = arg.arguments.mapIndexed { childIdx, childArg -> toNode(childArg, path + childIdx) },
        )

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
