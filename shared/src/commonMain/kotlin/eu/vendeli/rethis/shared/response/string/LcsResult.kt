package eu.vendeli.rethis.shared.response.string

/**
 * Result of `LCS key1 key2 IDX`.
 *
 * @property matches one entry per matching substring, in the order the server reports them.
 * @property totalLength length of the longest common subsequence.
 */
data class LcsResult(
    val matches: List<LcsMatch>,
    val totalLength: Long,
) {
    /**
     * A matching substring.
     *
     * @property first inclusive range of the match in the first key's value.
     * @property second inclusive range of the match in the second key's value.
     * @property length match length, present only when `WITHMATCHLEN` was requested.
     */
    data class LcsMatch(
        val first: LongRange,
        val second: LongRange,
        val length: Long? = null,
    )
}
