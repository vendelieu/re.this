package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("MEMORY STATS", RedisOperation.READ, [RespCode.ARRAY, RespCode.MAP])
fun interface MemoryStatsCommand : RedisCommandSpec<Map<String, RType>> {
    suspend fun encode(): CommandRequest<Nothing>
}
