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
import kotlinx.coroutines.test.runTest

class ConnectionCommandsTest : ReThisTestCtx() {
    @Test
    fun `test HELLO command with default parameters`() = runTest {
        client.hello().shouldNotBeNull().size shouldBeGreaterThan 1
    }

    @Test
    fun `test HELLO command with proto parameter`() = runTest {
        client.hello(proto = 2).shouldNotBeNull()["proto"] shouldBe Int64(2)
    }

    @Test
    fun `test HELLO command with username and password parameters`() = runTest {
        shouldThrow<ReThisException> {
            client.hello(
                username = "test",
                password = "test",
            )
        }.message shouldContain "WRONGPASS"
    }

    @Test
    fun `test HELLO command with name parameter`() = runTest {
        client.hello(name = "test").shouldNotBeNull().size shouldBeGreaterThan 1
    }

    @Test
    fun `test PING command with default message`() = runTest {
        val response = client.ping()
        response shouldBe "PONG"
    }

    @Test
    fun `test PING command with custom message`() = runTest {
        val message = "Hello, Redis!"
        val response = client.ping(message)
        response shouldBe message
    }

    @Test
    fun `test SELECT command with valid database index`() = runTest {
        val response = client.select(0)
        response shouldBe true
    }

    @Test
    suspend fun `test SELECT command with invalid database index`() = shouldThrow<ReThisException> {
        client.select(100)
    }
}
