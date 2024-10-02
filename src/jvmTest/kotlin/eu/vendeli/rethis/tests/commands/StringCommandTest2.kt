package eu.vendeli.rethis.tests.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.commands.*
import eu.vendeli.rethis.types.options.GetExOption
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

class StringCommandTest2 : ReThisTestCtx() {
    @Test
    suspend fun `test APPEND command`() {
        val origin = "testValue10"
        val appending = "newValue10"
        client.set("testKey10", origin)
        client.append("testKey10", appending) shouldBe (origin.length + appending.length)
    }

    @Test
    suspend fun `test DECR command`() {
        client.set("testKey11", "10")
        client.decr("testKey11") shouldBe 9L
    }

    @Test
    suspend fun `test DECRBY command`() {
        client.set("testKey12", "10")
        delay(200)
        client.decrBy("testKey12", 5) shouldBe 5L
    }

    @Test
    suspend fun `test GET command`() {
        client.set("testKey13", "testValue13")
        delay(200)
        client.get("testKey13") shouldBe "testValue13"
    }

    @Test
    suspend fun `test GETDEL command`() {
        client.set("testKey14", "testValue14")
        client.getDel("testKey14") shouldBe "testValue14"
    }

    @Test
    suspend fun `test GETEX command`() {
        client.set("testKey15", "testValue15")
        client.getEx("testKey15", GetExOption.EX(10.seconds)) shouldBe "testValue15"
    }

    @Test
    suspend fun `test GETRANGE command`() {
        client.set("testKey16", "testValue16")
        client.getRange("testKey16", 0L..4) shouldBe "testV"
    }

    @Test
    suspend fun `test INCR command`() {
        client.set("testKey17", "10")
        client.incr("testKey17") shouldBe 11L
    }

    @Test
    suspend fun `test INCRBY command`() {
        client.set("testKey18", "10")
        delay(200)
        client.incrBy("testKey18", 5) shouldBe 15L
    }

    @Test
    suspend fun `test INCRBYFLOAT command`() {
        client.set("testKey19", "10.5")
        delay(200)
        client.incrByFloat("testKey19", 5.5) shouldBe 16.0
    }
}
