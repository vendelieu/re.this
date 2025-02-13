package eu.vendeli.rethis.tests.utils

import com.ionspin.kotlin.bignum.integer.toBigInteger
import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.types.common.BigNumber
import eu.vendeli.rethis.types.common.Bool
import eu.vendeli.rethis.types.common.BulkString
import eu.vendeli.rethis.types.common.F64
import eu.vendeli.rethis.types.common.Int64
import eu.vendeli.rethis.types.common.PlainString
import eu.vendeli.rethis.types.common.Push
import eu.vendeli.rethis.types.common.RArray
import eu.vendeli.rethis.types.common.RMap
import eu.vendeli.rethis.types.common.RSet
import eu.vendeli.rethis.types.common.RType
import eu.vendeli.rethis.types.common.VerbatimString
import eu.vendeli.rethis.utils.response.parseResponse
import eu.vendeli.rethis.utils.response.readResponseWrapped
import eu.vendeli.rethis.utils.safeCast
import io.kotest.matchers.shouldBe
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer

class RTypeResponseTest : ReThisTestCtx() {
    @Test
    suspend fun `test readRedisMessage with simple string`() {
        val channel = ByteReadChannel {
            writeFully("+Hello, World!\r\n".encodeToByteArray())
        }

        val result = channel.parseResponse().readResponseWrapped(charset)
        result shouldBe PlainString("Hello, World!")
    }

    @Test
    suspend fun `test readRedisMessage with verbatim string`() {
        val channel = ByteReadChannel {
            writeFully("=15\r\ntxt:Some string\r\n".encodeToByteArray())
        }

        val result = channel.parseResponse().readResponseWrapped(charset)
        result shouldBe VerbatimString("txt", "Some string")
    }

    @Test
    suspend fun `test readRedisMessage with error`() {
        val channel = ByteReadChannel {
            writeFully("-Error message\r\n".encodeToByteArray())
        }

        val result = channel.parseResponse().readResponseWrapped(charset)

        result.safeCast<RType.Error>()?.exception?.message shouldBe "Error message"
    }

    @Test
    suspend fun `test readRedisMessage with bulk error`() {
        val channel = ByteReadChannel {
            writeFully("!21\r\nSYNTAX invalid syntax\r\n".encodeToByteArray())
        }

        channel
            .parseResponse()
            .readResponseWrapped(charset)
            .safeCast<RType.Error>()
            ?.exception
            ?.message shouldBe "SYNTAX invalid syntax"
    }

    @Test
    suspend fun `test readRedisMessage with integer`() {
        val channel = ByteReadChannel {
            writeFully(":123\r\n".encodeToByteArray())
        }

        val result = channel.parseResponse().readResponseWrapped(charset)
        result shouldBe Int64(123)
    }

    @Test
    suspend fun `test readRedisMessage with bulk string`() {
        val channel = ByteReadChannel {
            writeFully("$5\r\nHello\r\n".encodeToByteArray())
        }

        val result = channel.parseResponse().readResponseWrapped(charset)
        result shouldBe BulkString("Hello")
    }

    @Test
    suspend fun `test readRedisMessage with array`() {
        val channel = ByteReadChannel {
            writeFully("*2\r\n+first\r\n+second\r\n".encodeToByteArray())
        }

        val result = channel.parseResponse().readResponseWrapped(charset)
        result shouldBe RArray(
            listOf(
                PlainString("first"),
                PlainString("second"),
            ),
        )
    }

    @Test
    suspend fun `test readRedisMessage with null bulk string`() {
        val channel = ByteReadChannel {
            writeFully("$-1\r\n".encodeToByteArray())
        }

        val result = channel.parseResponse().readResponseWrapped(charset)
        result shouldBe RType.Null
    }

    @Test
    suspend fun `test readRedisMessage with null`() {
        val channel = ByteReadChannel {
            writeFully("_\r\n".encodeToByteArray())
        }

        val result = channel.parseResponse().readResponseWrapped(charset)
        result shouldBe RType.Null
    }

    @Test
    suspend fun `test readRedisMessage with null array`() {
        val channel = ByteReadChannel {
            writeFully("*-1\r\n".encodeToByteArray())
        }

        val result = channel.parseResponse().readResponseWrapped(charset)
        result shouldBe RType.Null
    }

    @Test
    suspend fun `test readRedisMessage with boolean`() {
        val channel = ByteReadChannel {
            writeFully("#t\r\n".encodeToByteArray())
        }

        val result = channel.parseResponse().readResponseWrapped(charset)
        result shouldBe Bool(true)
    }

    @Test
    suspend fun `test readRedisMessage with double`() {
        val channel = ByteReadChannel {
            writeFully(",3.14\r\n".encodeToByteArray())
        }

        val result = channel.parseResponse().readResponseWrapped(charset)
        result shouldBe F64(3.14)
    }

    @Test
    suspend fun `test readRedisMessage with big number`() {
        val channel = ByteReadChannel {
            writeFully("(12345678901234567890\r\n".encodeToByteArray())
        }

        val result = channel.parseResponse().readResponseWrapped(charset)
        result shouldBe BigNumber("12345678901234567890".toBigInteger())
    }

    @Test
    suspend fun `test readRedisMessage with set`() {
        val channel = ByteReadChannel {
            writeFully("~2\r\n$5\r\nHello\r\n$5\r\nWorld\r\n".encodeToByteArray())
        }

        val result = channel.parseResponse().readResponseWrapped(charset)
        result shouldBe RSet(setOf(BulkString("Hello"), BulkString("World")))
    }

    @Test
    suspend fun `test processRedisListResponse with push`() {
        val channel = ByteReadChannel {
            writeFully(">2\r\n$5\r\nHello\r\n$5\r\nWorld\r\n".encodeToByteArray())
        }

        val result = channel.parseResponse().readResponseWrapped(charset)
        result shouldBe Push(listOf(BulkString("Hello"), BulkString("World")))
    }

    @Test
    suspend fun `test processRedisListResponse with map`() {
        val channel = ByteReadChannel {
            writeFully("%2\r\n+first\r\n:1\r\n+second\r\n:2\r\n".encodeToByteArray())
        }

        val result = channel.parseResponse().readResponseWrapped(charset)
        result shouldBe RMap(
            mapOf(
                PlainString("first") to Int64(1),
                PlainString("second") to Int64(2),
            ),
        )
    }

    private val charset = Charsets.UTF_8

    @Suppress("TestFunctionName")
    private suspend fun ByteReadChannel(block: suspend Buffer.() -> Unit): ByteReadChannel {
        val buff = Buffer()
        buff.block()

        return ByteReadChannel(buff)
    }
}
