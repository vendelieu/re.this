package eu.vendeli.rethis.tests.utils

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.toBigInteger
import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.utils.processRedisListResponse
import eu.vendeli.rethis.utils.processRedisMapResponse
import eu.vendeli.rethis.utils.processRedisSimpleResponse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer

class ResponseUtilsTest : ReThisTestCtx() {
    private val charset = Charsets.UTF_8

    @Test
    suspend fun `test processRedisSimpleResponse with simple string`() {
        val channel = ByteReadChannel {
            writeFully("+Hello, World!\r\n".encodeToByteArray())
        }

        val result = channel.processRedisSimpleResponse<String>(charset)
        result shouldBe "Hello, World!"
    }

    @Test
    suspend fun `test processRedisSimpleResponse with verbatim string`() {
        val channel = ByteReadChannel {
            writeFully("=15\r\ntxt:Some string\r\n".encodeToByteArray())
        }

        val result = channel.processRedisSimpleResponse<String>(charset)
        result shouldBe "txt:Some string"
    }

    @Test
    suspend fun `test processRedisSimpleResponse with error`() {
        val channel = ByteReadChannel {
            writeFully("-Error message\r\n".encodeToByteArray())
        }

        shouldThrow<eu.vendeli.rethis.ReThisException> {
            channel.processRedisSimpleResponse<String>(charset)
        }.message shouldBe "Error message"
    }

    @Test
    suspend fun `test processRedisSimpleResponse with bulk error`() {
        val channel = ByteReadChannel {
            writeFully("!21\r\nSYNTAX invalid syntax\r\n".encodeToByteArray())
        }

        shouldThrow<eu.vendeli.rethis.ReThisException> {
            channel.processRedisSimpleResponse<String>(charset)
        }.message shouldBe "SYNTAX invalid syntax"
    }

    @Test
    suspend fun `test processRedisSimpleResponse with integer`() {
        val channel = ByteReadChannel {
            writeFully(":123\r\n".encodeToByteArray())
        }

        val result = channel.processRedisSimpleResponse<Long>(charset)
        result shouldBe 123L
    }

    @Test
    suspend fun `test processRedisSimpleResponse with bulk string`() {
        val channel = ByteReadChannel {
            writeFully("$5\r\nHello\r\n".encodeToByteArray())
        }

        val result = channel.processRedisSimpleResponse<String>(charset)
        result shouldBe "Hello"
    }

    @Test
    suspend fun `test processRedisSimpleResponse with null bulk string`() {
        val channel = ByteReadChannel {
            writeFully("$-1\r\n".encodeToByteArray())
        }

        val result = channel.processRedisSimpleResponse<String>(charset)
        result.shouldBeNull()
    }

    @Test
    suspend fun `test processRedisSimpleResponse with null`() {
        val channel = ByteReadChannel {
            writeFully("_\r\n".encodeToByteArray())
        }

        val result = channel.processRedisSimpleResponse<String>(charset)
        result.shouldBeNull()
    }

    @Test
    suspend fun `test processRedisSimpleResponse with boolean`() {
        val channel = ByteReadChannel {
            writeFully("#t\r\n".encodeToByteArray())
        }

        val result = channel.processRedisSimpleResponse<Boolean>(charset)
        result shouldBe true
    }

    @Test
    suspend fun `test processRedisSimpleResponse with double`() {
        val channel = ByteReadChannel {
            writeFully(",3.14\r\n".encodeToByteArray())
        }

        val result = channel.processRedisSimpleResponse<Double>(charset)
        result shouldBe 3.14
    }

    @Test
    suspend fun `test processRedisSimpleResponse with bignumber`() {
        val channel = ByteReadChannel {
            writeFully("(12345678901234567890\r\n".encodeToByteArray())
        }

        val result = channel.processRedisSimpleResponse<BigInteger>(charset)
        result shouldBe "12345678901234567890".toBigInteger()
    }

    @Test
    suspend fun `test processRedisListResponse with array`() {
        val channel = ByteReadChannel {
            writeFully("*2\r\n$5\r\nHello\r\n$5\r\nWorld\r\n".encodeToByteArray())
        }

        val result = channel.processRedisListResponse<String>(charset)
        result shouldBe listOf("Hello", "World")
    }

    @Test
    suspend fun `test processRedisListResponse with set`() {
        val channel = ByteReadChannel {
            writeFully("~2\r\n$5\r\nHello\r\n$5\r\nWorld\r\n".encodeToByteArray())
        }

        val result = channel.processRedisListResponse<String>(charset)
        result shouldBe listOf("Hello", "World")
    }

    @Test
    suspend fun `test processRedisListResponse with push`() {
        val channel = ByteReadChannel {
            writeFully(">2\r\n$5\r\nHello\r\n$5\r\nWorld\r\n".encodeToByteArray())
        }

        val result = channel.processRedisListResponse<String>(charset)
        result shouldBe listOf("Hello", "World")
    }

    @Test
    suspend fun `test processRedisListResponse with map`() {
        val channel = ByteReadChannel {
            writeFully("%2\r\n+first\r\n:1\r\n+second\r\n:2\r\n".encodeToByteArray())
        }

        val result = channel.processRedisMapResponse<String, Long>(charset)
        result shouldBe mapOf(
            "first" to 1L,
            "second" to 2L,
        )
    }

    @Suppress("TestFunctionName")
    private suspend fun ByteReadChannel(block: suspend Buffer.() -> Unit): ByteReadChannel {
        val buff = Buffer()
        buff.block()

        return ByteReadChannel(buff)
    }
}
