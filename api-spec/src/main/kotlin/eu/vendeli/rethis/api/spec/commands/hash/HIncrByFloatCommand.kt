package eu.vendeli.rethis.api.spec.commands.hash

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("HINCRBYFLOAT", RedisOperation.WRITE, [RespCode.BULK])
fun interface HIncrByFloatCommand : RedisCommandSpec<Double> {
    suspend fun encode(
        @RedisKey key: String,
        field: String,
        increment: Double
    ): CommandRequest<String>
}
