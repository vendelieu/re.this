package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("SWAPDB", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface SwapDbCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(index1: Long, index2: Long): CommandRequest
}
