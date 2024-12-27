package eu.vendeli.rethis.tests.commands

import eu.vendeli.rethis.ReThisException
import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.commands.hello
import eu.vendeli.rethis.commands.ping
import eu.vendeli.rethis.commands.select
import eu.vendeli.rethis.types.core.Int64
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class ConnectionCommandsTest : ReThisTestCtx() {
    @Test
    fun `test HELLO command with default parameters`(): Unit = runBlocking {
        client.hello().shouldNotBeNull().size shouldBeGreaterThan 1
    }

    @Test
    fun `test HELLO command with proto parameter`(): Unit = runBlocking {
        client.hello(proto = 2).shouldNotBeNull()["proto"] shouldBe Int64(2)
    }

    @Test
    fun `test HELLO command with username and password parameters`(): Unit = runBlocking {
        shouldThrow<ReThisException> {
            client.hello(
                username = "test",
                password = "test",
            )
        }.message shouldContain "WRONGPASS"
    }

    @Test
    fun `test HELLO command with name parameter`(): Unit = runBlocking {
        client.hello(name = "test").shouldNotBeNull().size shouldBeGreaterThan 1
    }

    @Test
    fun `test PING command with default message`(): Unit = runBlocking {
        val response = client.ping()
        response shouldBe "PONG"
    }

    @Test
    fun `test PING command with custom message`(): Unit = runBlocking {
        val message = "Hello, Redis!"
        val response = client.ping(message)
        response shouldBe message
    }

    @Test
    fun `test SELECT command with valid database index`(): Unit = runBlocking {
        val response = client.select(0)
        response shouldBe "OK"
    }

    @Test
    fun `test SELECT command with invalid database index`(): Unit = runBlocking {
        shouldThrow<ReThisException> {
            client.select(100)
        }
    }
}
