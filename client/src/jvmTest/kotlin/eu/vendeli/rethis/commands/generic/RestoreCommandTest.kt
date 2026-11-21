package eu.vendeli.rethis.commands.generic

import com.redis.testcontainers.RedisContainer
import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.TestEnv.TARGET_REDIS_VER
import eu.vendeli.rethis.command.generic.del
import eu.vendeli.rethis.command.generic.dump
import eu.vendeli.rethis.codecs.generic.RestoreCommandCodec
import eu.vendeli.rethis.command.generic.exists
import eu.vendeli.rethis.command.generic.objectIdleTime
import eu.vendeli.rethis.command.generic.pExpireTime
import eu.vendeli.rethis.command.generic.restore
import eu.vendeli.rethis.command.string.set
import eu.vendeli.rethis.shared.request.generic.RestoreOption
import eu.vendeli.rethis.shared.types.ReThisException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.longs.shouldBeInRange
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.io.readByteArray
import org.testcontainers.utility.DockerImageName
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

class RestoreCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test RESTORE command without options`() {
        client.set("testKey1", "testVal").shouldNotBeNull()
        val keyDump = client.dump("testKey1").shouldNotBeNull()
        client.del("testKey1")
        client.restore("testKey1", 0L, keyDump) shouldBe true
    }

    @Test
    suspend fun `test RESTORE command with REPLACE option`() {
        client.set("testKey2", "testVal").shouldNotBeNull()
        val keyDump = client.dump("testKey2").shouldNotBeNull()
        client.restore("testKey2", 10L, keyDump, RestoreOption.REPLACE) shouldBe true
    }

    @Test
    suspend fun `test RESTORE command with ABSTTL option`() {
        client.set("testKey3", "testVal").shouldNotBeNull()
        val keyDump = client.dump("testKey3").shouldNotBeNull()
        client.del("testKey3")
        val expireAtMillis = Clock.System.now().toEpochMilliseconds() + 1.days.inWholeMilliseconds
        client.restore("testKey3", expireAtMillis, keyDump, RestoreOption.ABSTTL) shouldBe true
        // The absolute expiry is stored as given, so it does not depend on how long the round trips took
        // or on the server clock relative to this one.
        client.pExpireTime("testKey3") shouldBe expireAtMillis
    }

    @Test
    suspend fun `test RESTORE command with ABSTTL option and past time`() {
        client.set("testKey3Past", "testVal").shouldNotBeNull()
        val keyDump = client.dump("testKey3Past").shouldNotBeNull()
        client.del("testKey3Past")
        client.restore("testKey3Past", 10L, keyDump, RestoreOption.ABSTTL) shouldBe true
        client.exists("testKey3Past") shouldBe 0L
    }

    @Test
    suspend fun `test RESTORE command with IDLETIME option`() {
        client.set("testKey4", "testVal").shouldNotBeNull()
        val keyDump = client.dump("testKey4").shouldNotBeNull()
        client.del("testKey4")
        // Negative values are rejected on the shared server, which proves the option reaches it.
        shouldThrow<ReThisException> {
            client.restore("testKey4", 0L, keyDump, RestoreOption.IdleTime((-10).seconds))
        }

        // The idle time is observed on a dedicated server: once any TimeSeries key has been created on a server
        // (the shared one is used by TS specs) the bundled RedisTimeSeries module touches every restored key and
        // OBJECT IDLETIME reads 0. Checked on redis:8.4.8: `RESTORE k 0 <dump> IDLETIME 10` gives 10 on a fresh
        // server and 0 after `TS.CREATE`; TTL 0 keeps the key alive; maxmemory-policy noeviction.
        val container = RedisContainer(DockerImageName.parse("redis:$TARGET_REDIS_VER"))
        try {
            container.start()
            val isolated = createClient(container.host, container.firstMappedPort)
            isolated.restore("testKey4", 0L, keyDump, RestoreOption.IdleTime(10.seconds)) shouldBe true
            isolated.objectIdleTime("testKey4").shouldNotBeNull() shouldBeInRange 10L..IDLETIME_TOLERANCE_SECONDS
        } finally {
            runCatching { container.stop() }
        }
    }

    @Test
    suspend fun `test RESTORE IDLETIME and FREQ are encoded as seconds and a plain number`() {
        val bytes = RestoreCommandCodec
            .encode(
                defaultCharset,
                "key",
                0L,
                "dump".encodeToByteArray(),
                RestoreOption.IdleTime(10.seconds),
                RestoreOption.Frequency(10L),
            ).data
            .readByteArray()
            .decodeToString()

        bytes shouldBe "*8\r\n\$7\r\nRESTORE\r\n\$3\r\nkey\r\n\$1\r\n0\r\n\$4\r\ndump\r\n" +
            "\$8\r\nIDLETIME\r\n\$2\r\n10\r\n\$4\r\nFREQ\r\n\$2\r\n10\r\n"
    }

    @Test
    suspend fun `test RESTORE command with FREQ option`() {
        client.set("testKey5", "testVal").shouldNotBeNull()
        val keyDump = client.dump("testKey5").shouldNotBeNull()
        client.del("testKey5")
        client.restore("testKey5", 10L, keyDump, RestoreOption.Frequency(10L)) shouldBe true

        // OBJECT FREQ needs an LFU maxmemory policy (the test server runs noeviction), and changing the policy
        // would affect other tests sharing the server. Instead prove the option reaches the server: FREQ above 255
        // is rejected, and would be accepted (the test would fail) if the codec dropped the option.
        client.del("testKey5")
        shouldThrow<ReThisException> {
            client.restore("testKey5", 10L, keyDump, RestoreOption.Frequency(256L))
        }
    }

    private companion object {
        const val IDLETIME_TOLERANCE_SECONDS = 30L
    }
}
