package eu.vendeli.rethis.api.spec.commands.sentinel

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.request.sentinel.SentinelSimulateFailureMode
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand(
    "SENTINEL SIMULATE-FAILURE",
    RedisOperation.WRITE,
    [RespCode.SIMPLE_STRING],
    extensions = [SentinelSimulateFailureMode::class],
)
fun interface SentinelSimulateFailureCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(mode: SentinelSimulateFailureMode): CommandRequest
}
