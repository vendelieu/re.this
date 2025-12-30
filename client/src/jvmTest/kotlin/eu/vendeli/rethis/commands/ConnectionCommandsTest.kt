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
    suspend fun `test HELLO command with default parameters`() {
        client.hello().shouldNotBeNull().size shouldBeGreaterThan 1
    }

    @Test
    suspend fun `test HELLO command with proto parameter`() {
        client.hello(protover = 2).shouldNotBeNull()["proto"] shouldBe Int64(2)
    }

    @Test
    suspend fun `test HELLO command with username and password parameters`() {
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
    suspend fun `test HELLO command with name parameter`() {
        client.hello(protover = 2, clientname = "test").shouldNotBeNull().size shouldBeGreaterThan 1
    }

    @Test
    suspend fun `test PING command with default message`() {
        val response = client.ping()
        response shouldBe "PONG"
    }

    @Test
    suspend fun `test PING command with custom message`() {
        val message = "Hello, Redis!"
        val response = client.ping(message)
        response shouldBe message
    }

    @Test
    suspend fun `test SELECT command with valid database index`() {
        val response = client.select(0)
        response shouldBe true
    }

    @Test
    suspend fun `test SELECT command with invalid database index`() = shouldThrow<ReThisException> {
        client.select(100)
    }
}
