package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.cast
import eu.vendeli.rethis.command.timeseries.tsAdd
import eu.vendeli.rethis.command.timeseries.tsCreate
import eu.vendeli.rethis.command.timeseries.tsMAdd
import eu.vendeli.rethis.command.timeseries.tsMRange
import eu.vendeli.rethis.command.timeseries.tsRange
import eu.vendeli.rethis.codecs.timeseries.TsRangeCommandCodec
import eu.vendeli.rethis.shared.request.timeseries.TsAggregator
import eu.vendeli.rethis.shared.request.timeseries.TsBucketTimestamp
import eu.vendeli.rethis.shared.request.timeseries.TsLabel
import io.kotest.matchers.string.shouldContain
import kotlinx.io.readString
import eu.vendeli.rethis.shared.request.timeseries.TsSample
import eu.vendeli.rethis.shared.types.F64
import eu.vendeli.rethis.shared.types.Int64
import eu.vendeli.rethis.shared.types.RArray
import eu.vendeli.rethis.shared.types.RMap
import io.kotest.matchers.shouldBe

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
        ) shouldBe RArray(
            listOf(
                RArray(listOf(Int64(100), F64(5.0))),
                RArray(listOf(Int64(200), F64(10.0))),
            ),
        )
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

    private suspend fun bucketRange(key: String, bt: TsBucketTimestamp?) = client.tsRange(
        key,
        "-",
        "+",
        latest = null,
        filterByTs = emptyList(),
        filterByValue = null,
        count = null,
        align = null,
        aggregator = TsAggregator.Avg,
        bucketDuration = 10L,
        bucketTimestamp = bt,
        empty = null,
    )

    private suspend fun seedBuckets(key: String) {
        client.tsCreate(key, null, null, null, null, TsLabel("bt", key)) shouldBe "OK"
        client.tsAdd(key, "101", 1.0, null, null, null, null)
        client.tsAdd(key, "105", 3.0, null, null, null, null)
    }

    private fun bucket(timestamp: Long) = RArray(listOf(RArray(listOf(Int64(timestamp), F64(2.0)))))

    @Test
    suspend fun `test TS_RANGE BUCKETTIMESTAMP reports the low, high or mid of the bucket`() {
        seedBuckets("tsBt1")

        bucketRange("tsBt1", null) shouldBe bucket(100)
        bucketRange("tsBt1", TsBucketTimestamp.Low) shouldBe bucket(100)
        bucketRange("tsBt1", TsBucketTimestamp.High) shouldBe bucket(110)
        bucketRange("tsBt1", TsBucketTimestamp.Mid) shouldBe bucket(105)
    }

    @Test
    suspend fun `test TS_MRANGE BUCKETTIMESTAMP reports the low, high or mid of the bucket`() {
        seedBuckets("tsBt2")

        suspend fun mRangeSamples(bt: TsBucketTimestamp) = client
            .tsMRange(
                "-",
                "+",
                filterByTs = emptyList(),
                selectedLabels = emptyList(),
                aggregator = TsAggregator.Avg,
                bucketDuration = 10L,
                bucketTimestamp = bt,
                filterExpr = arrayOf("bt=tsBt2"),
            ).cast<RMap>()
            .value.values
            .single()
            .cast<RArray>()
            .value
            .last()

        mRangeSamples(TsBucketTimestamp.Low) shouldBe bucket(100)
        mRangeSamples(TsBucketTimestamp.High) shouldBe bucket(110)
        mRangeSamples(TsBucketTimestamp.Mid) shouldBe bucket(105)
    }

    @Test
    suspend fun `test BUCKETTIMESTAMP codec writes the bt mode after the token`() {
        val encode = { bt: TsBucketTimestamp ->
            TsRangeCommandCodec
                .encode(
                    defaultCharset,
                    "k",
                    "-",
                    "+",
                    null,
                    emptyList(),
                    null,
                    null,
                    null,
                    TsAggregator.Avg,
                    10L,
                    bt,
                    null,
                ).data
                .readString()
        }
        encode(TsBucketTimestamp.Low) shouldContain "\$15\r\nBUCKETTIMESTAMP\r\n\$1\r\n-\r\n"
        encode(TsBucketTimestamp.High) shouldContain "\$15\r\nBUCKETTIMESTAMP\r\n\$1\r\n+\r\n"
        encode(TsBucketTimestamp.Mid) shouldContain "\$15\r\nBUCKETTIMESTAMP\r\n\$1\r\n~\r\n"
    }
}
