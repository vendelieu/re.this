package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.commands.scan
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.types.common.ScanResult
import eu.vendeli.rethis.types.options.ScanOption
import eu.vendeli.rethis.ReThisTestCtx
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
