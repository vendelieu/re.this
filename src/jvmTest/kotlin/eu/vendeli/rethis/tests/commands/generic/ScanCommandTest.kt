package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.commands.scan
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.types.common.ScanResult
import eu.vendeli.rethis.types.options.ScanOption
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ScanCommandTest : ReThisTestCtx() {
    @Test
    fun `test SCAN command without options`(): Unit = runTest {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.scan(0L).apply {
            cursor shouldBe "0"
            keys shouldContain "testKey"
        }
    }

    @Test
    fun `test SCAN command with MATCH option`(): Unit = runTest {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.scan(0L, ScanOption.Match("testKey*")) shouldBe ScanResult("0", listOf("testKey"))
    }

    @Test
    fun `test SCAN command with COUNT option`(): Unit = runTest {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.scan(0L, ScanOption.Count(10L)) shouldBe ScanResult("0", listOf("testKey"))
    }

    @Test
    fun `test SCAN command with TYPE option`(): Unit = runTest {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.scan(0L, ScanOption.Type("string")) shouldBe ScanResult("0", listOf("testKey"))
    }
}
