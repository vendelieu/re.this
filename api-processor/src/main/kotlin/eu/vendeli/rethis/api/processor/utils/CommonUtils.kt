package eu.vendeli.rethis.api.processor.utils

import eu.vendeli.rethis.api.processor.types.LibSpecNode
import eu.vendeli.rethis.api.processor.types.RedisCommandApiSpec

internal fun RedisCommandApiSpec.tryInferOperation(): String? = when {
    commandFlags?.any { it.equals("readonly", true) } == true -> "READ"
    commandFlags?.any { it.equals("write", true) } == true -> "WRITE"
    else -> null
}

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

internal fun printNodeChildren(
    node: LibSpecNode,
    indent: String = "",
) {
    // Print this node
    val label = when (node) {
        is LibSpecNode.ContainerNode -> "Container: ${node.symbol.simpleName.asString()}"
        is LibSpecNode.TokenNode -> "Token:     ${node.name}"
        is LibSpecNode.ParameterNode -> "Param:     ${node.name}"
    }
    println(indent + label)

    // Recurse into each child, increasing indentation
    node.children.forEach { child ->
        printNodeChildren(child, "$indent  ")
    }
}
