package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("COMMAND DOCS", RedisOperation.READ, [RespCode.ARRAY, RespCode.MAP])
fun interface CommandDocsCommand : RedisCommandSpec<List<Map<String, RType>>> {
    suspend fun encode(@RedisOptional vararg commandName: String): CommandRequest<Nothing>
}
