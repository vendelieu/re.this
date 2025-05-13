package eu.vendeli.rethis.api.spec.commands.sentinel

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("SENTINEL RESET", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface SentinelResetCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(pattern: String): CommandRequest<Nothing>
}
