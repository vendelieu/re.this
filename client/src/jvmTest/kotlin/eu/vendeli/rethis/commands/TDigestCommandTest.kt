package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.tdigest.tDigestAdd
import eu.vendeli.rethis.command.tdigest.tDigestCreate
import eu.vendeli.rethis.command.tdigest.tDigestReset
import eu.vendeli.rethis.shared.response.tdigest.TDigestValue
import io.kotest.matchers.shouldBe

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
}
