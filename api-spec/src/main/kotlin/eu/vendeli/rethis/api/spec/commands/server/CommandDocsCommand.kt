package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("COMMAND DOCS", RedisOperation.READ, [RespCode.ARRAY, RespCode.MAP])
fun interface CommandDocsCommand : RedisCommandSpec<Map<String, RType>> {
    suspend fun encode(vararg commandName: String): CommandRequest
}
