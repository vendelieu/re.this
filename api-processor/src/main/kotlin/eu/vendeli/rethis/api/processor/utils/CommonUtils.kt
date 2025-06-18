package eu.vendeli.rethis.api.processor.utils

import eu.vendeli.rethis.api.processor.types.EnrichedNode
import eu.vendeli.rethis.api.processor.types.EnrichedTreeAttr
import eu.vendeli.rethis.api.processor.types.RedisCommandApiSpec

internal fun RedisCommandApiSpec.tryInferOperation(): String? = when {
    commandFlags?.any { it.equals("readonly", true) } == true -> "READ"
    commandFlags?.any { it.equals("write", true) } == true -> "WRITE"
    else -> null
}

internal fun printEnrichedTree(root: EnrichedNode, indent: Int = 0) {
    val indentStr = "  ".repeat(indent)

    // Extract name safely
    val nodeName = root.attr.filterIsInstance<EnrichedTreeAttr.Name>().firstOrNull()?.name
        ?: root.attr.filterIsInstance<EnrichedTreeAttr.Symbol>().firstOrNull()?.symbol ?: "Unnamed"

    // Build attribute flags
    val flags = buildList {
        if (root.attr.any { it is EnrichedTreeAttr.Key }) add("K")
        if (root.attr.any { it is EnrichedTreeAttr.SizeParam }) add("S")
        root.attr.filterIsInstance<EnrichedTreeAttr.Optional>().firstOrNull()?.let {
            add("O[${it.inherited?.name?.firstOrNull() ?: '-'},${it.local?.name?.firstOrNull() ?: '-'}]")
        }
        root.attr.filterIsInstance<EnrichedTreeAttr.Multiple>().firstOrNull()?.let {
            add("M[${if (it.vararg) 'V' else '-'},${if (it.collection) 'C' else '-'}]")
        }
    }

    // Collect properties
    val properties = buildList {
        val tokens = root.tokens.map { it.name }
        if (tokens.isNotEmpty()) add("tokens=[${tokens.joinToString()}]")

        root.attr.filterIsInstance<EnrichedTreeAttr.Symbol>().firstOrNull()?.let {
            add("sym=${it.type.name.first()}(${it.symbol})")
        }

        if (root.rSpec != null) add("rSpec")
    }

    // Compose final output
    val flagStr = flags.joinToString(", ", "[", "]")
    val propStr = properties.joinToString(", ")
    println("$indentStr$nodeName $flagStr${if (propStr.isNotEmpty()) " ($propStr)" else ""}")

    // Print children recursively
    root.children.forEach { printEnrichedTree(it, indent + 1) }
}
