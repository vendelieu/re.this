package eu.vendeli.rethis.utils

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.toBigInteger
import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.shared.types.ReThisException
import eu.vendeli.rethis.shared.utils.readResponseWrapped
import eu.vendeli.rethis.shared.utils.unwrap
import eu.vendeli.rethis.shared.utils.unwrapList
import eu.vendeli.rethis.shared.utils.unwrapMap
import eu.vendeli.rethis.shared.utils.unwrapSet
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.ktor.utils.io.core.*

class ResponseUtilsTest : TestCtx() {
    @Test
    suspend fun `test processRedisSimpleResponse with simple string`() {
        val channel = Buffer {
            writeFully("+Hello, World!\r\n".encodeToByteArray())
        }

        val result = channel.readResponseWrapped().unwrap<String>()
        result shouldBe "Hello, World!"
    }

    @Test
    suspend fun `test processRedisSimpleResponse with verbatim string`() {
        val channel = Buffer {
            writeFully("=15\r\ntxt:Some string\r\n".encodeToByteArray())
        }

        val result = channel.readResponseWrapped().unwrap<String>()
        result shouldBe "txt:Some string"
    }

    @Test
    suspend fun `test processRedisSimpleResponse with error`() {
        val channel = Buffer {
            writeFully("-Error message\r\n".encodeToByteArray())
        }

        shouldThrow<ReThisException> {
            channel.readResponseWrapped().unwrap<String>()
        }.message shouldBe "Error message"
    }

    @Test
    suspend fun `test processRedisSimpleResponse with bulk error`() {
        val channel = Buffer {
            writeFully("!21\r\nSYNTAX invalid syntax\r\n".encodeToByteArray())
        }

        val result = channel.readResponseWrapped()
        shouldThrow<ReThisException> {
            result.unwrap<String>()
        }.message shouldBe "SYNTAX invalid syntax"
    }

    @Test
    suspend fun `test processRedisSimpleResponse with integer`() {
        val channel = Buffer {
            writeFully(":123\r\n".encodeToByteArray())
        }

        val result = channel.readResponseWrapped().unwrap<Long>()
        result shouldBe 123L
    }

    @Test
    suspend fun `test processRedisSimpleResponse with bulk string`() {
        val channel = Buffer {
            writeFully("$5\r\nHello\r\n".encodeToByteArray())
        }

        val result = channel.readResponseWrapped().unwrap<String>()
        result shouldBe "Hello"
    }

    @Test
    suspend fun `test processRedisSimpleResponse with null bulk string`() {
        val channel = Buffer {
            writeFully("$-1\r\n".encodeToByteArray())
        }

        val result = channel.readResponseWrapped().unwrap<String>()
        result.shouldBeNull()
    }

    @Test
    suspend fun `test processRedisSimpleResponse with null`() {
        val channel = Buffer {
            writeFully("_\r\n".encodeToByteArray())
        }

        val result = channel.readResponseWrapped().unwrap<String>()
        result.shouldBeNull()
    }

    @Test
    suspend fun `test processRedisSimpleResponse with boolean`() {
        val channel = Buffer {
            writeFully("#t\r\n".encodeToByteArray())
        }

        val result = channel.readResponseWrapped().unwrap<Boolean>()
        result shouldBe true
    }

    @Test
    suspend fun `test processRedisSimpleResponse with double`() {
        val channel = Buffer {
            writeFully(",3.14\r\n".encodeToByteArray())
        }

        val result = channel.readResponseWrapped().unwrap<Double>()
        result shouldBe 3.14
    }

    @Test
    suspend fun `test processRedisSimpleResponse with bignumber`() {
        val channel = Buffer {
            writeFully("(12345678901234567890\r\n".encodeToByteArray())
        }

        val result = channel.readResponseWrapped().unwrap<BigInteger>()
        result shouldBe "12345678901234567890".toBigInteger()
    }

    @Test
    suspend fun `test processRedisListResponse with array`() {
        val channel = Buffer {
            writeFully("*2\r\n$5\r\nHello\r\n$5\r\nWorld\r\n".encodeToByteArray())
        }

        val result = channel.readResponseWrapped().unwrapList<String>()
        result shouldBe listOf("Hello", "World")
    }

    @Test
    suspend fun `test processRedisListResponse with set`() {
        val channel = Buffer {
            writeFully("~2\r\n$5\r\nHello\r\n$5\r\nWorld\r\n".encodeToByteArray())
        }

        val result = channel.readResponseWrapped().unwrapSet<String>()
        result shouldBe setOf("Hello", "World")
    }

    @Test
    suspend fun `test processRedisListResponse with push`() {
        val channel = Buffer {
            writeFully(">2\r\n$5\r\nHello\r\n$5\r\nWorld\r\n".encodeToByteArray())
        }

        val result = channel.readResponseWrapped().unwrapList<String>()
        result shouldBe listOf("Hello", "World")
    }

    @Test
    suspend fun `test processRedisListResponse with map`() {
        val channel = Buffer {
            writeFully("%2\r\n+first\r\n:1\r\n+second\r\n:2\r\n".encodeToByteArray())
        }

        val result = channel.readResponseWrapped().unwrapMap<String, Long>()
        result shouldBe mapOf(
            "first" to 1L,
            "second" to 2L,
        )
    }
}
