package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.*

@RedisCommand("LATENCY LATEST", RedisOperation.READ, [RespCode.ARRAY])
fun interface LatencyLatestCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(): CommandRequest
}
