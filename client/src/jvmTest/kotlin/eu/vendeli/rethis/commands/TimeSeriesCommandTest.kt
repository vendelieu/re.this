package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.timeseries.tsAdd
import eu.vendeli.rethis.command.timeseries.tsCreate
import eu.vendeli.rethis.command.timeseries.tsMAdd
import eu.vendeli.rethis.command.timeseries.tsRange
import eu.vendeli.rethis.shared.request.timeseries.TsSample
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class TimeSeriesCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test TS_CREATE and TS_ADD commands`() {
        client.tsCreate("tsKey1", null, null, null, null) shouldBe "OK"
        client.tsAdd("tsKey1", "1000", 1.0, null, null, null, null) shouldBe 1000L
        client.tsAdd("tsKey1", "2000", 2.0, null, null, null, null) shouldBe 2000L
    }

    @Test
    suspend fun `test TS_RANGE command`() {
        client.tsCreate("tsKey2", null, null, null, null) shouldBe "OK"
        client.tsAdd("tsKey2", "100", 5.0, null, null, null, null)
        client.tsAdd("tsKey2", "200", 10.0, null, null, null, null)
        client.tsRange(
            "tsKey2",
            "-",
            "+",
            latest = null,
            filterByTs = emptyList(),
            filterByValue = null,
            count = null,
            align = null,
            aggregator = null,
            bucketDuration = null,
            bucketTimestamp = null,
            empty = null,
        ) shouldNotBe null
    }

    @Test
    suspend fun `test TS_MADD command`() {
        client.tsCreate("tsKey3a", null, null, null, null) shouldBe "OK"
        client.tsCreate("tsKey3b", null, null, null, null) shouldBe "OK"
        client
            .tsMAdd(
                TsSample("tsKey3a", "100", 1.0),
                TsSample("tsKey3b", "200", 2.0),
            ).size shouldBe 2
    }
}
