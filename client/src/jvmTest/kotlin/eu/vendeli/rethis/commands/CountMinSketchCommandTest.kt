package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.cms.cmsIncrBy
import eu.vendeli.rethis.command.cms.cmsInitByDim
import eu.vendeli.rethis.command.cms.cmsQuery
import eu.vendeli.rethis.shared.response.cms.CmsIncrement
import io.kotest.matchers.shouldBe

class CountMinSketchCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test CMS_INITBYDIM and CMS_INCRBY commands`() {
        client.cmsInitByDim("cmsKey1", 200L, 5L) shouldBe "OK"
        val res = client.cmsIncrBy(
            "cmsKey1",
            CmsIncrement("apple", 3L),
            CmsIncrement("banana", 2L),
        )
        res.size shouldBe 2
    }

    @Test
    suspend fun `test CMS_QUERY command`() {
        client.cmsInitByDim("cmsKey2", 200L, 5L) shouldBe "OK"
        client.cmsIncrBy("cmsKey2", CmsIncrement("x", 5L))
        client.cmsQuery("cmsKey2", "x", "missing").size shouldBe 2
    }
}
