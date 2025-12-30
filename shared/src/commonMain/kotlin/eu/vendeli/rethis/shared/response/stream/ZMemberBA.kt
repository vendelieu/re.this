package eu.vendeli.rethis.shared.response.stream

data class ZMemberBA(
    val member: ByteArray,
    val score: Double,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ZMemberBA

        if (score != other.score) return false
        if (!member.contentEquals(other.member)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = score.hashCode()
        result = 31 * result + member.contentHashCode()
        return result
    }
}
