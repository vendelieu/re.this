package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("LATENCY HISTOGRAM", RedisOperation.READ, [RespCode.ARRAY, RespCode.MAP])
fun interface LatencyHistogramCommand : RedisCommandSpec<Map<String, RType>> {
    suspend fun encode(@RedisOptional vararg command: String): CommandRequest
}
