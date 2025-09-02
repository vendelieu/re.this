package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.*

@RedisCommand("LATENCY HISTORY", RedisOperation.READ, [RespCode.ARRAY])
fun interface LatencyHistoryCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(event: String): CommandRequest
}
