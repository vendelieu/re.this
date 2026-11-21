package eu.vendeli.rethis.commands

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.codecs.connection.ClientKillCommandCodec
import eu.vendeli.rethis.codecs.generic.ExpireAtCommandCodec
import eu.vendeli.rethis.codecs.generic.ExpireCommandCodec
import eu.vendeli.rethis.codecs.generic.MigrateCommandCodec
import eu.vendeli.rethis.codecs.generic.PExpireAtCommandCodec
import eu.vendeli.rethis.codecs.generic.PExpireCommandCodec
import eu.vendeli.rethis.codecs.hash.HExpireAtCommandCodec
import eu.vendeli.rethis.codecs.hash.HExpireCommandCodec
import eu.vendeli.rethis.codecs.hash.HGetExCommandCodec
import eu.vendeli.rethis.codecs.hash.HPExpireAtCommandCodec
import eu.vendeli.rethis.codecs.hash.HPExpireCommandCodec
import eu.vendeli.rethis.codecs.hash.HSetExCommandCodec
import eu.vendeli.rethis.codecs.server.FailoverCommandCodec
import eu.vendeli.rethis.codecs.stream.XClaimCommandCodec
import eu.vendeli.rethis.codecs.stream.XReadGroupCommandCodec
import eu.vendeli.rethis.codecs.string.GetExCommandCodec
import eu.vendeli.rethis.codecs.string.MSetExCommandCodec
import eu.vendeli.rethis.codecs.string.SetCommandCodec
import eu.vendeli.rethis.shared.request.connection.ClientKillOptions
import eu.vendeli.rethis.shared.request.generic.MigrateKey
import eu.vendeli.rethis.shared.request.hash.HFieldValue
import eu.vendeli.rethis.shared.request.hash.HGetExOption
import eu.vendeli.rethis.shared.request.hash.HSetExOption
import eu.vendeli.rethis.shared.request.server.FailoverOptions
import eu.vendeli.rethis.shared.request.stream.XClaimOption
import eu.vendeli.rethis.shared.request.stream.XReadGroupKeyIds
import eu.vendeli.rethis.shared.request.stream.XReadGroupOption
import eu.vendeli.rethis.shared.request.string.GetExOption
import eu.vendeli.rethis.shared.request.string.KeyValue
import eu.vendeli.rethis.shared.request.string.MSetExOption
import eu.vendeli.rethis.shared.request.string.SetExpire
import eu.vendeli.rethis.shared.types.CommandRequest
import io.kotest.matchers.shouldBe
import kotlinx.io.readByteArray
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Pins the unit of every typed time argument on the wire against the Redis documentation: relative options and
 * `*AT` options are written in the unit named by their token, never in another one.
 */
class TimeUnitWireTest : TestCtx() {
    private val cs = Charsets.UTF_8
    private val atSeconds = Instant.fromEpochSeconds(1_800_000_000)
    private val atMillis = Instant.fromEpochMilliseconds(1_800_000_000_123)

    private fun CommandRequest.args(): List<String> = data
        .readByteArray()
        .decodeToString()
        .split("\r\n")
        .filter { it.isNotEmpty() && it[0] != '*' && it[0] != '$' }

    @Test
    fun `SET expiry options use the unit of their token`() {
        fun set(opt: SetExpire) = SetCommandCodec.encode(cs, "k", "v", opt).args()

        set(SetExpire.Ex(90.seconds)) shouldBe listOf("SET", "k", "v", "EX", "90")
        set(SetExpire.Px(1500.milliseconds)) shouldBe listOf("SET", "k", "v", "PX", "1500")
        set(SetExpire.ExAt(atSeconds)) shouldBe listOf("SET", "k", "v", "EXAT", "1800000000")
        set(SetExpire.PxAt(atMillis)) shouldBe listOf("SET", "k", "v", "PXAT", "1800000000123")
    }

    @Test
    fun `GETEX expiry options use the unit of their token`() {
        fun getEx(opt: GetExOption) = GetExCommandCodec.encode(cs, "k", opt).args()

        getEx(GetExOption.Ex(90.seconds)) shouldBe listOf("GETEX", "k", "EX", "90")
        getEx(GetExOption.Px(1500.milliseconds)) shouldBe listOf("GETEX", "k", "PX", "1500")
        getEx(GetExOption.ExAt(atSeconds)) shouldBe listOf("GETEX", "k", "EXAT", "1800000000")
        getEx(GetExOption.PxAt(atMillis)) shouldBe listOf("GETEX", "k", "PXAT", "1800000000123")
    }

    @Test
    fun `MSETEX expiry options use the unit of their token`() {
        fun mSetEx(exp: MSetExOption.Expiration) =
            MSetExCommandCodec.encode(cs, KeyValue("k", "v"), condition = null, expiration = exp).args().takeLast(2)

        mSetEx(MSetExOption.Expiration.Ex(90.seconds)) shouldBe listOf("EX", "90")
        mSetEx(MSetExOption.Expiration.Px(1500.milliseconds)) shouldBe listOf("PX", "1500")
        mSetEx(MSetExOption.Expiration.ExAt(atSeconds)) shouldBe listOf("EXAT", "1800000000")
        mSetEx(MSetExOption.Expiration.PxAt(atMillis)) shouldBe listOf("PXAT", "1800000000123")
    }

    @Test
    fun `EXPIRE family uses seconds or milliseconds as named`() {
        ExpireCommandCodec.encode(cs, "k", 90.seconds, null).args() shouldBe listOf("EXPIRE", "k", "90")
        PExpireCommandCodec.encode(cs, "k", 1500.milliseconds, null).args() shouldBe listOf("PEXPIRE", "k", "1500")
        ExpireAtCommandCodec.encode(cs, "k", atSeconds, null).args() shouldBe listOf("EXPIREAT", "k", "1800000000")
        PExpireAtCommandCodec.encode(cs, "k", atMillis, null).args() shouldBe
            listOf("PEXPIREAT", "k", "1800000000123")
    }

    @Test
    fun `HEXPIRE family uses seconds or milliseconds as named`() {
        HExpireCommandCodec.encode(cs, "k", 90.seconds, "f", condition = null).args() shouldBe
            listOf("HEXPIRE", "k", "90", "FIELDS", "1", "f")
        HPExpireCommandCodec.encode(cs, "k", 1500.milliseconds, null, "f").args() shouldBe
            listOf("HPEXPIRE", "k", "1500", "FIELDS", "1", "f")
        HExpireAtCommandCodec.encode(cs, "k", atSeconds, "f", condition = null).args() shouldBe
            listOf("HEXPIREAT", "k", "1800000000", "FIELDS", "1", "f")
        HPExpireAtCommandCodec.encode(cs, "k", atMillis, null, "f").args() shouldBe
            listOf("HPEXPIREAT", "k", "1800000000123", "FIELDS", "1", "f")
    }

    @Test
    fun `HGETEX and HSETEX expiry options use the unit of their token`() {
        fun hGetEx(opt: HGetExOption) = HGetExCommandCodec
            .encode(cs, "k", opt, "f")
            .args()
            .drop(2)
            .take(2)
        hGetEx(HGetExOption.Ex(90.seconds)) shouldBe listOf("EX", "90")
        hGetEx(HGetExOption.Px(1500.milliseconds)) shouldBe listOf("PX", "1500")
        hGetEx(HGetExOption.ExAt(atSeconds)) shouldBe listOf("EXAT", "1800000000")
        hGetEx(HGetExOption.PxAt(atMillis)) shouldBe listOf("PXAT", "1800000000123")

        fun hSetEx(exp: HSetExOption.Expiration) =
            HSetExCommandCodec
                .encode(cs, "k", null, exp, HFieldValue("f", "v"))
                .args()
                .drop(2)
                .take(2)
        hSetEx(HSetExOption.Expiration.Ex(90.seconds)) shouldBe listOf("EX", "90")
        hSetEx(HSetExOption.Expiration.Px(1500.milliseconds)) shouldBe listOf("PX", "1500")
        hSetEx(HSetExOption.Expiration.ExAt(atSeconds)) shouldBe listOf("EXAT", "1800000000")
        hSetEx(HSetExOption.Expiration.PxAt(atMillis)) shouldBe listOf("PXAT", "1800000000123")
    }

    @Test
    fun `MIGRATE timeout is written in milliseconds`() {
        MigrateCommandCodec.encode(cs, "h", 6379L, MigrateKey.Actual("k"), 0L, 1500.milliseconds).args() shouldBe
            listOf("MIGRATE", "h", "6379", "k", "0", "1500")
    }

    @Test
    fun `blocking options are written in milliseconds`() {
        val args = XReadGroupCommandCodec
            .encode(cs, "g", "c", XReadGroupKeyIds(listOf("s"), listOf(">")), XReadGroupOption.Block(1500.milliseconds))
            .args()
        args.take(2) shouldBe listOf("XREADGROUP", "GROUP")
        args.slice(args.indexOf("BLOCK")..args.indexOf("BLOCK") + 1) shouldBe listOf("BLOCK", "1500")
    }

    @Test
    fun `XCLAIM TIME is a unix time in milliseconds`() {
        val args = XClaimCommandCodec
            .encode(
                cs,
                "s",
                "g",
                "c",
                "0",
                "1-0",
                idle = null,
                time = XClaimOption.Time(atMillis),
                retryCount = null,
                force = null,
                justId = null,
                lastId = null,
            ).args()
        args.slice(args.indexOf("TIME")..args.indexOf("TIME") + 1) shouldBe listOf("TIME", "1800000000123")
    }

    @Test
    fun `FAILOVER TIMEOUT is written in milliseconds`() {
        FailoverCommandCodec.encode(cs, FailoverOptions.Timeout(1500.milliseconds)).args() shouldBe
            listOf("FAILOVER", "TIMEOUT", "1500")
    }

    @Test
    fun `CLIENT KILL MAXAGE is an age in seconds, not a point in time`() {
        ClientKillCommandCodec.encode(cs, ClientKillOptions.MaxAge(90.seconds)).args() shouldBe
            listOf("CLIENT", "KILL", "MAXAGE", "90")
    }
}
