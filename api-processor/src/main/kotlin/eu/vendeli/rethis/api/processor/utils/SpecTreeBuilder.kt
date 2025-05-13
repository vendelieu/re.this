package eu.vendeli.rethis.api.processor.utils

import eu.vendeli.rethis.api.processor.type.CommandArgument
import eu.vendeli.rethis.api.processor.type.SpecNode

internal class SpecTreeBuilder(private val raw: List<CommandArgument>) {
    fun build(): List<SpecNode> = raw.map { toNode(it) }

    private fun toNode(arg: CommandArgument, parentNode: SpecNode? = null): SpecNode = when (arg.type) {
        "oneof" -> SpecNode.OneOf(
            arg.name,
            arg.arguments.map { toNode(it, parentNode) },
            arg.optional,
            arg.multiple,
            parentNode
        )
        "block" -> SpecNode.Block(
            arg.name,
            arg.token,
            arg.arguments.map { toNode(it, parentNode) },
            arg.optional,
            arg.multiple,
            parentNode
        )
        "pure-token" -> SpecNode.PureToken(
            arg.name,
            arg.token!!,
            parentNode
        )
        else -> SpecNode.Simple(
            arg.type,
            arg.name,
            arg.optional,
            arg.multiple,
            parentNode
        )
    }
}
