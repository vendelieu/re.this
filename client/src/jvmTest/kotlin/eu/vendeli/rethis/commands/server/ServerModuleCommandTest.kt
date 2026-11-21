package eu.vendeli.rethis.commands.server

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.server.moduleList
import eu.vendeli.rethis.commands.mapField
import eu.vendeli.rethis.shared.types.BulkString
import io.kotest.matchers.collections.shouldContainAll

class ServerModuleCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `MODULE LIST returns modules`() {
        val names = client.moduleList().map { it.mapField("name") }
        names shouldContainAll listOf("search", "bf", "timeseries", "ReJSON").map { BulkString(it) }
    }
}
