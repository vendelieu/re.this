package eu.vendeli.rethis.api.processor.utils

import eu.vendeli.rethis.api.processor.type.LibSpecTree
import eu.vendeli.rethis.api.processor.type.RedisCommandApiSpec

internal fun RedisCommandApiSpec.tryInferOperation(): String? = when {
    commandFlags?.any { it.equals("readonly", true) } == true -> "READ"
    commandFlags?.any { it.equals("write", true) } == true -> "WRITE"
    else -> null
}

internal fun printNodePath(node: LibSpecTree) {
    // Build the chain from this node up to the root
    val path = generateSequence(node) { it.parent }.toList().asReversed()
    // Print each element, indenting by depth
    path.forEachIndexed { depth, n ->
        val indent = "  ".repeat(depth)
        val label = when (n) {
            is LibSpecTree.ContainerNode -> "Container: ${n.symbol.simpleName.asString()}"
            is LibSpecTree.TokenNode -> "Token:     ${n.name}"
            is LibSpecTree.ParameterNode -> "Param:     ${n.name}"
        }
        println(indent + label)
    }
}

internal fun printNodeChildren(
    node: LibSpecTree,
    indent: String = "",
) {
    // Print this node
    val label = when (node) {
        is LibSpecTree.ContainerNode -> "Container: ${node.symbol.simpleName.asString()}"
        is LibSpecTree.TokenNode -> "Token:     ${node.name}"
        is LibSpecTree.ParameterNode -> "Param:     ${node.name}"
    }
    println(indent + label)

    // Recurse into each child, increasing indentation
    node.children.forEach { child ->
        printNodeChildren(child, "$indent  ")
    }
}
