package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.shared.request.generic.SortOption
import eu.vendeli.rethis.command.generic.sortRo
import eu.vendeli.rethis.command.list.rPush
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class SortRoCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test SORT_RO command without options`() {
        client.rPush("testKey1", "1.0", "1.2", "2.0").shouldNotBeNull()
        client.sortRo("testKey1") shouldBe listOf("1.0", "1.2", "2.0")
    }

    @Test
    suspend fun `test SORT_RO command with BY option`() {
        client.rPush("testKey2", "testVal1", "testVal2", "testVal3").shouldNotBeNull()
        client.sortRo("testKey2", SortOption.By("testKey*")) shouldBe listOf("testVal1", "testVal2", "testVal3")
    }

    @Test
    suspend fun `test SORT_RO command with LIMIT option`() {
        client.rPush("testKey3", "1.0", "1.2", "2.0").shouldNotBeNull()
        client.sortRo("testKey3", SortOption.Limit(1L, 10L)) shouldBe listOf("1.2", "2.0")
    }

    @Test
    suspend fun `test SORT_RO command with GET option`() {
        client.rPush("testKey4", "1.0", "1.2", "2.0").shouldNotBeNull()
        client.sortRo("testKey4", SortOption.Get("#")) shouldBe listOf("1.0", "1.2", "2.0")
    }

    @Test
    suspend fun `test SORT_RO command with ASC option`() {
        client.rPush("testKey5", "1.0", "1.2", "2.0").shouldNotBeNull()
        client.sortRo("testKey5", SortOption.ASC) shouldBe listOf("1.0", "1.2", "2.0")
    }

    @Test
    suspend fun `test SORT_RO command with DESC option`() {
        client.rPush("testKey6", "1.0", "1.2", "2.0").shouldNotBeNull()
        client.sortRo("testKey6", SortOption.DESC) shouldBe listOf("2.0", "1.2", "1.0")
    }

    @Test
    suspend fun `test SORT_RO command with ALPHA option`() {
        client.rPush("testKey7", "testVal1", "testVal2", "testVal3").shouldNotBeNull()
        client.sortRo("testKey7", SortOption.ALPHA) shouldBe listOf("testVal1", "testVal2", "testVal3")
    }
}
