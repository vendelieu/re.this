package eu.vendeli.rethis.api.processor.utils

import eu.vendeli.rethis.api.processor.type.LibSpecNode

internal fun printNodePath(node: LibSpecNode) {
    // Build the chain from this node up to the root
    val path = generateSequence(node) { it.parent }.toList().asReversed()
    // Print each element, indenting by depth
    path.forEachIndexed { depth, n ->
        val indent = "  ".repeat(depth)
        val label = when (n) {
            is LibSpecNode.ContainerNode -> "Container: ${n.symbol.simpleName.asString()}"
            is LibSpecNode.TokenNode -> "Token:     ${n.name}"
            is LibSpecNode.ParameterNode -> "Param:     ${n.name}"
        }
        println(indent + label)
    }
}
