package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("MEMORY MALLOC-STATS", RedisOperation.READ, [RespCode.BULK])
fun interface MemoryMallocStatsCommand : RedisCommandSpec<String> {
    suspend fun encode(): CommandRequest
}
