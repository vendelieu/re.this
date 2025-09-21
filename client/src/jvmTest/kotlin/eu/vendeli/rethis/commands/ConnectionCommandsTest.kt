package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.connection.hello
import eu.vendeli.rethis.command.connection.ping
import eu.vendeli.rethis.command.connection.select
import eu.vendeli.rethis.shared.request.connection.HelloAuth
import eu.vendeli.rethis.shared.types.Int64
import eu.vendeli.rethis.shared.types.ReThisException
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
        client.hello(protover = 2).shouldNotBeNull()["proto"] shouldBe Int64(2)
    }

    @Test
    fun `test HELLO command with username and password parameters`() = runTest {
        shouldThrow<ReThisException> {
            client.hello(
                protover = 2,
                auth = HelloAuth(
                    username = "test",
                    password = "test".toCharArray(),
                ),
            )
        }.message shouldContain "WRONGPASS"
    }

    @Test
    fun `test HELLO command with name parameter`() = runTest {
        client.hello(protover = 2, clientname = "test").shouldNotBeNull().size shouldBeGreaterThan 1
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
