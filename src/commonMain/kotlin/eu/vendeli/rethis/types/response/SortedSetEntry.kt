package eu.vendeli.rethis.types.response

/**
 * Represents an entry in a Redis Sorted Set.
 *
 * @param member The member of the sorted set.
 * @param score The score associated with the member.
 */
data class SortedSetEntry(
    val member: String,
    val score: Double,
)
