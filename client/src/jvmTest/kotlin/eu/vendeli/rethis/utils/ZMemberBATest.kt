package eu.vendeli.rethis.utils

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.shared.response.sortedset.ZMemberBA
import io.kotest.matchers.shouldBe

class ZMemberBATest : TestCtx() {
    private val member = "m".encodeToByteArray()

    @Test
    suspend fun `positive and negative zero scores are distinct`() {
        (ZMemberBA(member, 0.0) == ZMemberBA(member, -0.0)) shouldBe false
        setOf(ZMemberBA(member, 0.0), ZMemberBA(member, -0.0)).size shouldBe 2
    }

    @Test
    suspend fun `nan scores are equal`() {
        (ZMemberBA(member, Double.NaN) == ZMemberBA(member, Double.NaN)) shouldBe true
    }

    @Test
    suspend fun `same member and score are equal`() {
        (ZMemberBA(member, 1.5) == ZMemberBA("m".encodeToByteArray(), 1.5)) shouldBe true
    }

    @Test
    suspend fun `equal instances share a hash code`() {
        val scores = listOf(0.0, -0.0, Double.NaN, 1.5, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)

        for (left in scores) {
            for (right in scores) {
                val a = ZMemberBA(member, left)
                val b = ZMemberBA(member, right)
                if (a == b) a.hashCode() shouldBe b.hashCode()
            }
        }
    }
}
