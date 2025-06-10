package eu.vendeli.rethis.api.spec.commands.hyperloglog

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("PFADD", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface PfAddCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        key: String,
        @RedisOptional vararg element: String
    ): CommandRequest
}
