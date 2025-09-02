package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("LATENCY GRAPH", RedisOperation.READ, [RespCode.BULK])
fun interface LatencyGraphCommand : RedisCommandSpec<String> {
    suspend fun encode(event: String): CommandRequest
}
