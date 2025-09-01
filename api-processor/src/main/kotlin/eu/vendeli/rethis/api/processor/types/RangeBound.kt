package eu.vendeli.rethis.api.processor.types

internal data class RangeBound(val path: List<Int>) : Comparable<RangeBound> {
    override fun compareTo(other: RangeBound): Int {
        for (i in 0 until minOf(path.size, other.path.size)) {
            val c = path[i].compareTo(other.path[i])
            if (c != 0) return c
        }
        return path.size.compareTo(other.path.size)
    }
}

internal fun EnrichedNode.rangeBounds(): Pair<RangeBound, RangeBound> {
    val myPath = rSpec?.path.orEmpty()

    // If this node has its own spec path, prefer it as authoritative.
    // This prevents children (e.g., enum token variants bound elsewhere) from skewing the parent's bounds.
    if (myPath.isNotEmpty()) {
        val b = RangeBound(myPath)
        return b to b
    }

    if (children.isEmpty()) {
        val b = RangeBound(myPath)
        return b to b
    }

    // No own spec path — derive bounds from children.
    val childBounds = children.map { it.rangeBounds() }
    val mins = childBounds.minByOrNull { it.first }!!.first
    val maxs = childBounds.maxByOrNull { it.second }!!.second
    return mins to maxs
}
