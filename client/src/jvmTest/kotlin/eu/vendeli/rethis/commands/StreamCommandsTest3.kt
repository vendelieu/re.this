package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.stream.xAckDel
import eu.vendeli.rethis.command.stream.xAdd
import eu.vendeli.rethis.command.stream.xDelEx
import eu.vendeli.rethis.command.stream.xGroupCreate
import eu.vendeli.rethis.shared.request.common.FieldValue
import eu.vendeli.rethis.shared.request.stream.XAddOption
import eu.vendeli.rethis.shared.request.stream.XId
import io.kotest.matchers.collections.shouldHaveSize

class StreamCommandsTest3 : ReThisTestCtx() {
    @Test
    suspend fun `test XACKDEL command`() {
        val streamKey = "xackdelStream"
        client.xGroupCreate(streamKey, "g1", XId.Id("0"), mkstream = true)
        val id = client.xAdd(streamKey, idSelector = XAddOption.Asterisk, data = arrayOf(FieldValue("k", "v")))
            ?: error("XADD returned null id")
        client.xAckDel(streamKey, "g1", null, id) shouldHaveSize 1
    }

    @Test
    suspend fun `test XDELEX command`() {
        val streamKey = "xdelexStream"
        val id = client.xAdd(streamKey, idSelector = XAddOption.Asterisk, data = arrayOf(FieldValue("k", "v")))
            ?: error("XADD returned null id")
        client.xDelEx(streamKey, null, id) shouldHaveSize 1
    }
}
