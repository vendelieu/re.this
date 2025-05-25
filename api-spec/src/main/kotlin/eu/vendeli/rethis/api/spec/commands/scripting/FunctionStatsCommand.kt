package eu.vendeli.rethis.api.spec.commands.scripting

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("FUNCTION STATS", RedisOperation.READ, [RespCode.MAP, RespCode.ARRAY])
fun interface FunctionStatsCommand : RedisCommandSpec<Map<String, RType?>> {
    suspend fun encode(): CommandRequest<Nothing>
}
