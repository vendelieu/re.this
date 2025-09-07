package eu.vendeli.rethis.utils

import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeBlank
import kotlinx.coroutines.test.runTest
import kotlinx.io.Buffer
import kotlinx.io.readString
import java.util.*

class RawReqUtilsTest : ReThisTestCtx() {
    private fun Buffer.readAllUtf8(): String = readString()

    @Test
    fun `toRESPBuffer encodes ECHO properly`() = runTest {
        val payload = listOf("ECHO", "hi").toRESPBuffer()
        val encoded = payload.readAllUtf8()
        encoded shouldBe "*2\r\n\$4\r\nECHO\r\n\$2\r\nhi\r\n"
    }

    @Test
    fun `toRESPBuffer encodes null as Null Bulk String`() = runTest {
        val payload = listOf("ECHO", null).toRESPBuffer()
        val encoded = payload.readAllUtf8()
        encoded shouldBe "*2\r\n\$4\r\nECHO\r\n\$-1\r\n"
    }

    @Test
    fun `execute with prepared RESP buffer returns expected bulk string for ECHO`() = runTest {
        val req = listOf("ECHO", "ok").toRESPBuffer()
        val resp = client.execute(req)
        val raw = resp.readAllUtf8()
        // Expect bulk string with "ok"
        raw shouldBe "\$2\r\nok\r\n"
    }

    @Test
    fun `execute block helper builds buffer and PING succeeds`() = runTest {
        val resp = client.execute {
            add("PING")
        }
        val raw = resp.readAllUtf8()
        // Simple string PONG
        raw shouldBe "+PONG\r\n"
    }

    @Test
    fun `toRESPBuffer supports numbers and byte arrays with INCRBY and ECHO`() = runTest {
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
    fun `execute block can send multi-arg commands`() = runTest {
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
    fun `execute with explicit operationKind and isBlocking flags`() = runTest {
        // Use PING — flags should not affect correctness for a simple command
        val resp = client.execute(
            request = listOf("PING").toRESPBuffer(),
            operationKind = eu.vendeli.rethis.shared.types.RedisOperation.READ,
            isBlocking = false,
        )
        resp.readAllUtf8() shouldBe "+PONG\r\n"
    }

    @Test
    fun `QUIT a temporary client using low-level execute`() = runTest {
        val tmp = createClient()

        val quitReq = listOf("QUIT").toRESPBuffer()
        val resp = tmp.execute(quitReq).readAllUtf8()
        // QUIT returns +OK and then closes the connection
        resp shouldBe "+OK\r\n"

    }
}
