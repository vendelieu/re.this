package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.vector.vAdd
import eu.vendeli.rethis.command.vector.vCard
import eu.vendeli.rethis.command.vector.vDim
import eu.vendeli.rethis.command.vector.vEmb
import eu.vendeli.rethis.command.vector.vGetAttr
import eu.vendeli.rethis.command.vector.vInfo
import eu.vendeli.rethis.command.vector.vIsMember
import eu.vendeli.rethis.command.vector.vLinks
import eu.vendeli.rethis.command.vector.vRandMember
import eu.vendeli.rethis.command.vector.vRange
import eu.vendeli.rethis.command.vector.vRem
import eu.vendeli.rethis.command.vector.vSetAttr
import eu.vendeli.rethis.command.vector.vSim
import eu.vendeli.rethis.shared.request.vector.VAddInput
import eu.vendeli.rethis.shared.request.vector.VSimSource
import eu.vendeli.rethis.shared.types.RType
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@Suppress("ktlint:standard:function-naming")
class VectorCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test VADD command`() {
        client.vAdd("vk1", VAddInput.Values(1.0, 0.0, 0.0), "elem-a").shouldBeTrue()
    }

    @Test
    suspend fun `test VCARD command`() {
        client.vCard("vcard-empty") shouldBe 0L
        client.vAdd("vcard-k", VAddInput.Values(1.0, 0.0, 0.0), "a")
        client.vAdd("vcard-k", VAddInput.Values(0.0, 1.0, 0.0), "b")
        client.vCard("vcard-k") shouldBe 2L
    }

    @Test
    suspend fun `test VDIM command`() {
        client.vAdd("vdim-k", VAddInput.Values(1.0, 0.0, 0.0), "a")
        client.vDim("vdim-k") shouldBe 3L
    }

    @Test
    suspend fun `test VINFO command`() {
        client.vInfo("vinfo-empty") shouldBe RType.Null
        client.vAdd("vinfo-k", VAddInput.Values(1.0, 0.0, 0.0), "a")
        client.vInfo("vinfo-k") shouldNotBe RType.Null
    }

    @Test
    suspend fun `test VEMB command`() {
        client.vAdd("vemb-k", VAddInput.Values(1.0, 0.0, 0.0), "a")
        client.vEmb("vemb-k", "a", null) shouldNotBe RType.Null
    }

    @Test
    suspend fun `test VSETATTR and VGETATTR commands`() {
        client.vAdd("vattr-k", VAddInput.Values(1.0, 0.0, 0.0), "a")
        client.vSetAttr("vattr-k", "a", "{\"label\":\"first\"}").shouldBeTrue()
        client.vGetAttr("vattr-k", "a") shouldBe "{\"label\":\"first\"}"
    }

    @Test
    suspend fun `test VGETATTR command on non-existent element`() {
        client.vGetAttr("vgetattr-empty", "elem-a").shouldBeNull()
    }

    @Test
    suspend fun `test VREM command`() {
        client.vRem("vrem-empty", "elem-a").shouldBeFalse()
        client.vAdd("vrem-k", VAddInput.Values(1.0, 0.0, 0.0), "a")
        client.vRem("vrem-k", "a").shouldBeTrue()
    }

    @Test
    suspend fun `test VRANDMEMBER command`() {
        client.vRandMember("vrand-empty", null) shouldBe RType.Null
        client.vAdd("vrand-k", VAddInput.Values(1.0, 0.0, 0.0), "a")
        client.vRandMember("vrand-k", null) shouldNotBe RType.Null
    }

    @Test
    suspend fun `test VISMEMBER command`() {
        client.vIsMember("vismember-empty", "elem-a").shouldBeFalse()
        client.vAdd("vismember-k", VAddInput.Values(1.0, 0.0, 0.0), "a")
        client.vIsMember("vismember-k", "a").shouldBeTrue()
    }

    @Test
    suspend fun `test VSIM command`() {
        client.vAdd("vsim-k", VAddInput.Values(1.0, 0.0, 0.0), "a")
        client.vAdd("vsim-k", VAddInput.Values(0.9, 0.1, 0.0), "b")
        client.vSim("vsim-k", VSimSource.Ele("a")) shouldNotBe RType.Null
    }

    @Test
    suspend fun `test VLINKS command`() {
        client.vAdd("vlinks-k", VAddInput.Values(1.0, 0.0, 0.0), "a")
        client.vLinks("vlinks-k", "a", null) shouldNotBe RType.Null
    }

    @Test
    suspend fun `test VRANGE command`() {
        client.vAdd("vrange-k", VAddInput.Values(1.0, 0.0, 0.0), "alpha")
        client.vAdd("vrange-k", VAddInput.Values(0.9, 0.1, 0.0), "beta")
        client.vRange("vrange-k", "-", "+", null) shouldNotBe null
    }
}
