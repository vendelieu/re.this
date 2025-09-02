package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.*

@RedisCommand("LATENCY HISTOGRAM", RedisOperation.READ, [RespCode.ARRAY, RespCode.MAP])
fun interface LatencyHistogramCommand : RedisCommandSpec<Map<String, RType>> {
    suspend fun encode(vararg command: String): CommandRequest
}
