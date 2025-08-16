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
    val myPath = rSpec?.path ?: emptyList()
    if (children.isEmpty()) {
        val b = RangeBound(myPath)
        return b to b
    }
    val childBounds = children.map { it.rangeBounds() }
    val mins = childBounds.minByOrNull { it.first }!!.first
    val maxs = childBounds.maxByOrNull { it.second }!!.second
    if (mins.path.isEmpty() && maxs.path.isEmpty() && myPath.isNotEmpty()) {
        return RangeBound(myPath).let { it to it }
    }
    return mins to maxs
}
