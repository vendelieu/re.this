package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.generic.scan
import eu.vendeli.rethis.command.string.set
import eu.vendeli.rethis.shared.request.generic.ScanOption
import eu.vendeli.rethis.shared.response.common.ScanResult
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class ScanCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test SCAN command without options`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.scan(0L).apply {
            cursor shouldBe "0"
            keys shouldContain "testKey"
        }
    }

    @Test
    suspend fun `test SCAN command with MATCH option`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.scan(0L, ScanOption.Match("testKey*")) shouldBe ScanResult("0", listOf("testKey"))
    }

    @Test
    suspend fun `test SCAN command with COUNT option`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.scan(0L, ScanOption.Count(10L)) shouldBe ScanResult("0", listOf("testKey"))
    }

    @Test
    suspend fun `test SCAN command with TYPE option`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.scan(0L, ScanOption.Type("string")) shouldBe ScanResult("0", listOf("testKey"))
    }
}
