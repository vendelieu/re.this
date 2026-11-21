package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.codecs.tdigest.TDigestByRankCommandCodec
import eu.vendeli.rethis.codecs.tdigest.TDigestByRevRankCommandCodec
import eu.vendeli.rethis.command.tdigest.tDigestAdd
import eu.vendeli.rethis.command.tdigest.tDigestByRank
import eu.vendeli.rethis.command.tdigest.tDigestByRevRank
import eu.vendeli.rethis.command.tdigest.tDigestCreate
import eu.vendeli.rethis.command.tdigest.tDigestReset
import eu.vendeli.rethis.shared.response.tdigest.TDigestValue
import eu.vendeli.rethis.shared.types.F64
import eu.vendeli.rethis.shared.types.RArray
import io.kotest.matchers.shouldBe
import kotlinx.io.readString

class TDigestCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test TDIGEST_CREATE and TDIGEST_ADD commands`() {
        client.tDigestCreate("tdKey1", 100L) shouldBe "OK"
        client.tDigestAdd(
            "tdKey1",
            TDigestValue(1.0),
            TDigestValue(2.0),
            TDigestValue(3.0),
        ) shouldBe "OK"
    }

    @Test
    suspend fun `test TDIGEST_RESET command`() {
        client.tDigestCreate("tdKey2", 100L) shouldBe "OK"
        client.tDigestAdd("tdKey2", TDigestValue(10.0)) shouldBe "OK"
        client.tDigestReset("tdKey2") shouldBe "OK"
    }

    @Test
    suspend fun `test TDIGEST_BYRANK and TDIGEST_BYREVRANK return the min and max`() {
        client.tDigestCreate("tdKey3", 100L) shouldBe "OK"
        client.tDigestAdd("tdKey3", *(1..10).map { TDigestValue(it.toDouble()) }.toTypedArray()) shouldBe "OK"

        client.tDigestByRank("tdKey3", 0L, 9L) shouldBe RArray(listOf(F64(1.0), F64(10.0)))
        client.tDigestByRevRank("tdKey3", 0L, 9L) shouldBe RArray(listOf(F64(10.0), F64(1.0)))
    }

    @Test
    suspend fun `test TDIGEST_BYRANK and TDIGEST_BYREVRANK codecs write integer ranks`() {
        TDigestByRankCommandCodec.encode(defaultCharset, "k", 0L, 1L).data.readString() shouldBe
            "*4\r\n\$14\r\nTDIGEST.BYRANK\r\n\$1\r\nk\r\n\$1\r\n0\r\n\$1\r\n1\r\n"
        TDigestByRevRankCommandCodec.encode(defaultCharset, "k", 0L, 1L).data.readString() shouldBe
            "*4\r\n\$17\r\nTDIGEST.BYREVRANK\r\n\$1\r\nk\r\n\$1\r\n0\r\n\$1\r\n1\r\n"
    }
}
