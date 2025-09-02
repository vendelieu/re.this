package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.*

@RedisCommand("COMMAND DOCS", RedisOperation.READ, [RespCode.ARRAY, RespCode.MAP])
fun interface CommandDocsCommand : RedisCommandSpec<Map<String, RType>> {
    suspend fun encode(vararg commandName: String): CommandRequest
}
