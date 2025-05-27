package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.commands.rPush
import eu.vendeli.rethis.commands.sortRo
import eu.vendeli.rethis.types.options.SortRoOption
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
        client.sortRo("testKey2", SortRoOption.BY("testKey*")) shouldBe listOf("testVal1", "testVal2", "testVal3")
    }

    @Test
    suspend fun `test SORT_RO command with LIMIT option`() {
        client.rPush("testKey3", "1.0", "1.2", "2.0").shouldNotBeNull()
        client.sortRo("testKey3", SortRoOption.LIMIT(1L, 10L)) shouldBe listOf("1.2", "2.0")
    }

    @Test
    suspend fun `test SORT_RO command with GET option`() {
        client.rPush("testKey4", "1.0", "1.2", "2.0").shouldNotBeNull()
        client.sortRo("testKey4", SortRoOption.GET("#")) shouldBe listOf("1.0", "1.2", "2.0")
    }

    @Test
    suspend fun `test SORT_RO command with ASC option`() {
        client.rPush("testKey5", "1.0", "1.2", "2.0").shouldNotBeNull()
        client.sortRo("testKey5", SortRoOption.ASC) shouldBe listOf("1.0", "1.2", "2.0")
    }

    @Test
    suspend fun `test SORT_RO command with DESC option`() {
        client.rPush("testKey6", "1.0", "1.2", "2.0").shouldNotBeNull()
        client.sortRo("testKey6", SortRoOption.DESC) shouldBe listOf("2.0", "1.2", "1.0")
    }

    @Test
    suspend fun `test SORT_RO command with ALPHA option`() {
        client.rPush("testKey7", "testVal1", "testVal2", "testVal3").shouldNotBeNull()
        client.sortRo("testKey7", SortRoOption.ALPHA) shouldBe listOf("testVal1", "testVal2", "testVal3")
    }
}
