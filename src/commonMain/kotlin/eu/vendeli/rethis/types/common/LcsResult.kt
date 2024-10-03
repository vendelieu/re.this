package eu.vendeli.rethis.types.common

data class LcsResult(
    val matches: List<List<LcsMatch>>,
    val totalLength: Long,
) {
    data class LcsMatch(
        val start: Long,
        val end: Long,
        val length: Long? = null,
    )
}