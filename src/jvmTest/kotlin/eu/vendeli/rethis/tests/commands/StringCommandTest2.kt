package eu.vendeli.rethis.tests.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.commands.*
import eu.vendeli.rethis.types.options.GetExOption
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class StringCommandTest2 : ReThisTestCtx() {
    @Test
    fun `test APPEND command`(): Unit = runBlocking {
        val origin = "testValue10"
        val appending = "newValue10"
        client.set("testKey10", origin)
        client.append("testKey10", appending) shouldBe (origin.length + appending.length)
    }

    @Test
    fun `test DECR command`(): Unit = runBlocking {
        client.set("testKey11", "10")
        client.decr("testKey11") shouldBe 9L
    }

    @Test
    fun `test DECRBY command`(): Unit = runBlocking {
        client.set("testKey12", "10")
        client.decrBy("testKey12", 5) shouldBe 5L
    }

    @Test
    fun `test GET command`(): Unit = runBlocking {
        client.set("testKey13", "testValue13")
        client.get("testKey13") shouldBe "testValue13"
    }

    @Test
    fun `test GETDEL command`(): Unit = runBlocking {
        client.set("testKey14", "testValue14")
        client.getDel("testKey14") shouldBe "testValue14"
    }

    @Test
    fun `test GETEX command`(): Unit = runBlocking {
        client.set("testKey15", "testValue15")
        client.getEx("testKey15", GetExOption.EX(10.seconds)) shouldBe "testValue15"
    }

    @Test
    fun `test GETRANGE command`(): Unit = runBlocking {
        client.set("testKey16", "testValue16")
        client.getRange("testKey16", 0L..4) shouldBe "testV"
    }

    @Test
    fun `test INCR command`(): Unit = runBlocking {
        client.set("testKey17", "10")
        client.incr("testKey17") shouldBe 11L
    }

    @Test
    fun `test INCRBY command`(): Unit = runBlocking {
        client.set("testKey18", "10")
        client.incrBy("testKey18", 5) shouldBe 15L
    }

    @Test
    fun `test INCRBYFLOAT command`(): Unit = runBlocking {
        client.set("testKey19", "10.5")
        client.incrByFloat("testKey19", 5.5) shouldBe 16.0
    }
}
