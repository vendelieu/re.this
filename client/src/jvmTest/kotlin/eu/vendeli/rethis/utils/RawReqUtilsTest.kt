package eu.vendeli.rethis.utils

import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeBlank
import kotlinx.io.Buffer
import kotlinx.io.readString
import java.util.*

class RawReqUtilsTest : ReThisTestCtx() {
    private fun Buffer.readAllUtf8(): String = readString()

    @Test
    fun `toRESPBuffer encodes ECHO properly`() {
        val payload = listOf("ECHO", "hi").toRESPBuffer()
        val encoded = payload.readAllUtf8()
        encoded shouldBe "*2\r\n\$4\r\nECHO\r\n\$2\r\nhi\r\n"
    }

    @Test
    fun `toRESPBuffer encodes null as Null Bulk String`() {
        val payload = listOf("ECHO", null).toRESPBuffer()
        val encoded = payload.readAllUtf8()
        encoded shouldBe "*2\r\n\$4\r\nECHO\r\n\$-1\r\n"
    }

    @Test
    suspend fun `execute with prepared RESP buffer returns expected bulk string for ECHO`() {
        val req = listOf("ECHO", "ok").toRESPBuffer()
        val resp = client.execute(req)
        val raw = resp.readAllUtf8()
        // Expect bulk string with "ok"
        raw shouldBe "\$2\r\nok\r\n"
    }

    @Test
    suspend fun `execute block helper builds buffer and PING succeeds`() {
        val resp = client.execute {
            add("PING")
        }
        val raw = resp.readAllUtf8()
        // Simple string PONG
        raw shouldBe "+PONG\r\n"
    }

    @Test
    suspend fun `toRESPBuffer supports numbers and byte arrays with INCRBY and ECHO`() {
        // Use a fresh key to avoid collisions
        val key = "raw:req:int:" + UUID.randomUUID()
        // INCRBY key by Int(2)
        val incrReq = listOf("INCRBY", key, 2).toRESPBuffer()
        val incrResp = client.execute(incrReq).readAllUtf8()
        incrResp shouldContain "2\r\n" // RESP integer is ":2\r\n" (RESP2/3)

        // ECHO with ByteArray
        val bytes = "bin".encodeToByteArray()
        val echoReq = listOf("ECHO", bytes).toRESPBuffer()
        val echoResp = client.execute(echoReq).readAllUtf8()
        echoResp shouldBe "\$3\r\nbin\r\n"
    }

    @Test
    suspend fun `execute block can send multi-arg commands`() {
        val key = "raw:req:kv:" + UUID.randomUUID()
        // SET key value
        val setResp = client.execute {
            add("SET"); add(key); add("v")
        }.readAllUtf8()
        setResp.shouldNotBeNull().shouldNotBeBlank()
        setResp shouldBe "+OK\r\n"

        // GET key
        val getResp = client.execute {
            add("GET"); add(key)
        }.readAllUtf8()
        getResp shouldBe "\$1\r\nv\r\n"
    }

    @Test
    suspend fun `execute with explicit operationKind and isBlocking flags`() {
        // Use PING — flags should not affect correctness for a simple command
        val resp = client.execute(
            request = listOf("PING").toRESPBuffer(),
            operationKind = eu.vendeli.rethis.shared.types.RedisOperation.READ,
            isBlocking = false,
        )
        resp.readAllUtf8() shouldBe "+PONG\r\n"
    }

    @Test
    suspend fun `QUIT a temporary client using low-level execute`() {
        val tmp = createClient()

        val resp = tmp.execute(listOf("QUIT").toRESPBuffer()).readAllUtf8()
        // QUIT returns +OK and then closes the connection
        resp shouldBe "+OK\r\n"
    }
}
