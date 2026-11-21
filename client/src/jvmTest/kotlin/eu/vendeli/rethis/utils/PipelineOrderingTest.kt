package eu.vendeli.rethis.utils

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.Int64
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.RedisOperation
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import kotlinx.io.Buffer

class PipelineOrderingTest : TestCtx() {
    private fun request(name: String, slot: Int?): CommandRequest =
        CommandRequest(Buffer(), RedisOperation.READ, command = name).also { r -> slot?.let { r.withSlot(it) } }

    // a fake router: replies to each request with the position encoded in its command name
    private fun reply(
        requests: List<CommandRequest>,
    ): List<RType> = requests.map { Int64(it.command.removePrefix("c").toLong()) }

    private fun order(requests: List<CommandRequest>): List<Long> = runBlocking {
        executeBySlot(requests) { _, group -> reply(group) }.map { (it as Int64).value }
    }

    @Test
    fun `interleaved slots return responses in request order`() {
        order(listOf(request("c0", 1), request("c1", 2), request("c2", 1))) shouldBe listOf(0L, 1L, 2L)
    }

    @Test
    fun `requests without slots keep request order`() {
        order(listOf(request("c0", null), request("c1", null), request("c2", null))) shouldBe listOf(0L, 1L, 2L)
    }

    @Test
    fun `single slot is unchanged`() {
        order(listOf(request("c0", 5), request("c1", 5))) shouldBe listOf(0L, 1L)
    }

    @Test
    fun `each slot group is executed once with its requests in order`() {
        val calls = mutableListOf<Pair<Int?, List<String>>>()
        runBlocking {
            executeBySlot(listOf(request("c0", 1), request("c1", 2), request("c2", 1))) { slot, group ->
                calls += slot to group.map { it.command }
                reply(group)
            }
        }
        calls shouldBe listOf(1 to listOf("c0", "c2"), 2 to listOf("c1"))
    }
}
