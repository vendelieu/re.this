package eu.vendeli.rethis.utils

import com.ionspin.kotlin.bignum.integer.toBigInteger
import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.shared.types.*
import eu.vendeli.rethis.shared.utils.readResponseWrapped
import io.kotest.matchers.shouldBe
import io.ktor.utils.io.core.*

class RTypeResponseTest : TestCtx() {
    @Test
    suspend fun `test readRedisMessage with simple string`() {
        val channel = Buffer {
            writeFully("+Hello, World!\r\n".encodeToByteArray())
        }

        val result = channel.readResponseWrapped(defaultCharset)
        result shouldBe PlainString("Hello, World!")
    }

    @Test
    suspend fun `test readRedisMessage with verbatim string`() {
        val channel = Buffer {
            writeFully("=15\r\ntxt:Some string\r\n".encodeToByteArray())
        }

        val result = channel.readResponseWrapped(defaultCharset)
        result shouldBe VerbatimString("txt", "Some string")
    }

    @Test
    suspend fun `test readRedisMessage with error`() {
        val channel = Buffer {
            writeFully("-Error message\r\n".encodeToByteArray())
        }

        val result = channel.readResponseWrapped(defaultCharset)

        result.safeCast<RType.Error>()?.exception?.message shouldBe "Error message"
    }

    @Test
    suspend fun `test readRedisMessage with bulk error`() {
        val channel = Buffer {
            writeFully("!21\r\nSYNTAX invalid syntax\r\n".encodeToByteArray())
        }

        channel
            .readResponseWrapped(defaultCharset)
            .safeCast<RType.Error>()
            ?.exception
            ?.message shouldBe "SYNTAX invalid syntax"
    }

    @Test
    suspend fun `test readRedisMessage with integer`() {
        val channel = Buffer {
            writeFully(":123\r\n".encodeToByteArray())
        }

        val result = channel.readResponseWrapped(defaultCharset)
        result shouldBe Int64(123)
    }

    @Test
    suspend fun `test readRedisMessage with bulk string`() {
        val channel = Buffer {
            writeFully("$5\r\nHello\r\n".encodeToByteArray())
        }

        val result = channel.readResponseWrapped(defaultCharset)
        result shouldBe BulkString("Hello")
    }

    @Test
    suspend fun `test readRedisMessage with array`() {
        val channel = Buffer {
            writeFully("*2\r\n+first\r\n+second\r\n".encodeToByteArray())
        }

        val result = channel.readResponseWrapped(defaultCharset)
        result shouldBe RArray(
            listOf(
                PlainString("first"),
                PlainString("second"),
            ),
        )
    }

    @Test
    suspend fun `test readRedisMessage with null bulk string`() {
        val channel = Buffer {
            writeFully("$-1\r\n".encodeToByteArray())
        }

        val result = channel.readResponseWrapped(defaultCharset)
        result shouldBe RType.Null
    }

    @Test
    suspend fun `test readRedisMessage with null`() {
        val channel = Buffer {
            writeFully("_\r\n".encodeToByteArray())
        }

        val result = channel.readResponseWrapped(defaultCharset)
        result shouldBe RType.Null
    }

    @Test
    suspend fun `test readRedisMessage with null array`() {
        val channel = Buffer {
            writeFully("*-1\r\n".encodeToByteArray())
        }

        val result = channel.readResponseWrapped(defaultCharset)
        result shouldBe RType.Null
    }

    @Test
    suspend fun `test readRedisMessage with boolean`() {
        val channel = Buffer {
            writeFully("#t\r\n".encodeToByteArray())
        }

        val result = channel.readResponseWrapped(defaultCharset)
        result shouldBe Bool(true)
    }

    @Test
    suspend fun `test readRedisMessage with double`() {
        val channel = Buffer {
            writeFully(",3.14\r\n".encodeToByteArray())
        }

        val result = channel.readResponseWrapped(defaultCharset)
        result shouldBe F64(3.14)
    }

    @Test
    suspend fun `test readRedisMessage with big number`() {
        val channel = Buffer {
            writeFully("(12345678901234567890\r\n".encodeToByteArray())
        }

        val result = channel.readResponseWrapped(defaultCharset)
        result shouldBe BigNumber("12345678901234567890".toBigInteger())
    }

    @Test
    suspend fun `test readRedisMessage with set`() {
        val channel = Buffer {
            writeFully("~2\r\n$5\r\nHello\r\n$5\r\nWorld\r\n".encodeToByteArray())
        }

        val result = channel.readResponseWrapped(defaultCharset)
        result shouldBe RSet(setOf(BulkString("Hello"), BulkString("World")))
    }

    @Test
    suspend fun `test processRedisListResponse with push`() {
        val channel = Buffer {
            writeFully(">2\r\n$5\r\nHello\r\n$5\r\nWorld\r\n".encodeToByteArray())
        }

        val result = channel.readResponseWrapped(defaultCharset)
        result shouldBe Push(listOf(BulkString("Hello"), BulkString("World")))
    }

    @Test
    suspend fun `test processRedisListResponse with map`() {
        val channel = Buffer {
            writeFully("%2\r\n+first\r\n:1\r\n+second\r\n:2\r\n".encodeToByteArray())
        }

        val result = channel.readResponseWrapped(defaultCharset)
        result shouldBe RMap(
            mapOf(
                PlainString("first") to Int64(1),
                PlainString("second") to Int64(2),
            ),
        )
    }
}
