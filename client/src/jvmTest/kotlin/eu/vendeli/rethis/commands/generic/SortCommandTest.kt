package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.shared.request.generic.SortOption
import eu.vendeli.rethis.command.generic.sort
import eu.vendeli.rethis.command.generic.sortStore
import eu.vendeli.rethis.command.set.sAdd
import io.kotest.matchers.shouldBe

class SortCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test SORT command without options`() {
        client.sAdd("testKey1", "0.0") shouldBe 1L
        client.sort("testKey1") shouldBe listOf("0.0")
    }

    @Test
    suspend fun `test SORT command with BY option`() {
        client.sAdd("testKey2", "0.0") shouldBe 1L
        client.sort("testKey2", SortOption.By("testKey2*")) shouldBe listOf("0.0")
    }

    @Test
    suspend fun `test SORT command with LIMIT option`() {
        client.sAdd("testKey3", "0.0") shouldBe 1L
        client.sort("testKey3", SortOption.Limit(0L, 10L)) shouldBe listOf("0.0")
    }

    @Test
    suspend fun `test SORT command with GET option`() {
        client.sAdd("testKey4", "1.0") shouldBe 1L
        client.sort("testKey4", SortOption.Get("#")) shouldBe listOf("1.0")
    }

    @Test
    suspend fun `test SORT command with ASC option`() {
        client.sAdd("testKey5", "0.0") shouldBe 1L
        client.sort("testKey5", SortOption.ASC) shouldBe listOf("0.0")
    }

    @Test
    suspend fun `test SORT command with DESC option`() {
        client.sAdd("testKey6", "0.0") shouldBe 1L
        client.sort("testKey6", SortOption.DESC) shouldBe listOf("0.0")
    }

    @Test
    suspend fun `test SORT command with ALPHA option`() {
        client.sAdd("testKey7", "0.0") shouldBe 1L
        client.sort("testKey7", SortOption.ALPHA) shouldBe listOf("0.0")
    }

    @Test
    suspend fun `test SORT command with STORE option`() {
        client.sAdd("testKey8", "0.0") shouldBe 1L
        client.sortStore("testKey8", "destination") shouldBe 1L
    }
}
