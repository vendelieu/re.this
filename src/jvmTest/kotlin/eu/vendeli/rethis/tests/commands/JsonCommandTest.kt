package eu.vendeli.rethis.tests.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.commands.*
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class JsonCommandTest : ReThisTestCtx(true) {
    @Test
    fun `test JSON_ARRAPPEND command`(): Unit = runTest {
        client.jsonSet("testKey1", ".", "[1, 2, 3]")
        client.jsonArrAppend("testKey1", ".", "4", "5") shouldBe 5L
    }

    @Test
    fun `test JSON_ARRINDEX command`(): Unit = runTest {
        client.jsonSet("testKey2", ".", "[1, 2, 3]")
        client.jsonArrIndex("testKey2", ".", "2") shouldBe 1L
    }

    @Test
    fun `test JSON_ARRINSERT command`(): Unit = runTest {
        client.jsonSet("testKey3", ".", "[1, 2, 3]")
        client.jsonArrInsert("testKey3", ".", 1, "4", "5") shouldBe 5L
    }

    @Test
    fun `test JSON_ARRLEN command`(): Unit = runTest {
        client.jsonSet("testKey4", ".", "[1, 2, 3]")
        client.jsonArrLen("testKey4", ".") shouldBe 3L
    }

    @Test
    fun `test JSON_ARRPOP command`(): Unit = runTest {
        client.jsonSet("testKey5", ".", "[1, 2, 3]")
        client.jsonArrPop("testKey5", ".") shouldBe "3"
    }

    @Test
    fun `test JSON_ARRTRIM command`(): Unit = runTest {
        client.jsonSet("testKey6", ".", "[1, 2, 3]")

        client.jsonArrTrim("testKey6", ".", 1, 1) shouldBe 1L
    }

    @Test
    fun `test JSON_CLEAR command`(): Unit = runTest {
        client.jsonSet("testKey7", ".", "[1, 2, 3]")
        client.jsonClear("testKey7") shouldBe 1L
    }

    @Test
    fun `test JSON_DEL command`(): Unit = runTest {
        client.jsonSet("testKey9", ".", "[1, 2, 3]")
        client.jsonDel("testKey9", ".") shouldBe 1L
    }

    @Test
    fun `test JSON_FORGET command`(): Unit = runTest {
        client.jsonSet("testKey10", ".", "[1, 2, 3]")
        client.jsonForget("testKey10") shouldBe 1L
    }

    @Test
    fun `test JSON_GET command`(): Unit = runTest {
        client.jsonSet("testKey11", ".", "[1, 2, 3]")
        client.jsonGet("testKey11") shouldBe "[1,2,3]"
    }

    @Test
    fun `test JSON_MERGE command`(): Unit = runTest {
        client.jsonSet("testKey12", ".", "[1, 2, 3]")
        client.jsonMerge("testKey12", ".", "[4, 5, 6]") shouldBe "OK"
    }
}
