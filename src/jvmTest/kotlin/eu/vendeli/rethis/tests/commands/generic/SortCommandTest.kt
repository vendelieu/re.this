package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.commands.sAdd
import eu.vendeli.rethis.commands.sort
import eu.vendeli.rethis.types.options.SortOption
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class SortCommandTest : ReThisTestCtx() {
    @Test
    fun `test SORT command without options`(): Unit = runBlocking {
        client.sAdd("testKey1", "0.0") shouldBe 1L
        client.sort("testKey1") shouldBe listOf("0.0")
    }

    @Test
    fun `test SORT command with BY option`(): Unit = runBlocking {
        client.sAdd("testKey2", "0.0") shouldBe 1L
        client.sort("testKey2", SortOption.BY("testKey2*")) shouldBe listOf("0.0")
    }

    @Test
    fun `test SORT command with LIMIT option`(): Unit = runBlocking {
        client.sAdd("testKey3", "0.0") shouldBe 1L
        client.sort("testKey3", SortOption.LIMIT(0L, 10L)) shouldBe listOf("0.0")
    }

    @Test
    fun `test SORT command with GET option`(): Unit = runBlocking {
        client.sAdd("testKey4", "1.0") shouldBe 1L
        client.sort("testKey4", SortOption.GET("#")) shouldBe listOf("1.0")
    }

    @Test
    fun `test SORT command with ASC option`(): Unit = runBlocking {
        client.sAdd("testKey5", "0.0") shouldBe 1L
        client.sort("testKey5", SortOption.ASC) shouldBe listOf("0.0")
    }

    @Test
    fun `test SORT command with DESC option`(): Unit = runBlocking {
        client.sAdd("testKey6", "0.0") shouldBe 1L
        client.sort("testKey6", SortOption.DESC) shouldBe listOf("0.0")
    }

    @Test
    fun `test SORT command with ALPHA option`(): Unit = runBlocking {
        client.sAdd("testKey7", "0.0") shouldBe 1L
        client.sort("testKey7", SortOption.ALPHA) shouldBe listOf("0.0")
    }

    @Test
    fun `test SORT command with STORE option`(): Unit = runBlocking {
        client.sAdd("testKey8", "0.0") shouldBe 1L
        client.sort("testKey8", SortOption.STORE("destination")) shouldBe 1L
    }
}
