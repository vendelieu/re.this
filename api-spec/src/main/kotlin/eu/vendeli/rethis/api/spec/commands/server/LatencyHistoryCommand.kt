package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("LATENCY HISTORY", RedisOperation.READ, [RespCode.ARRAY])
fun interface LatencyHistoryCommand : RedisCommandSpec<List<Map<String, RType>>> {
    suspend fun encode(event: String): CommandRequest<Nothing>
}
