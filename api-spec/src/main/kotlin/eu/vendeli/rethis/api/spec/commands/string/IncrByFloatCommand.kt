package eu.vendeli.rethis.api.spec.commands.string

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("INCRBYFLOAT", RedisOperation.WRITE, [RespCode.DOUBLE])
fun interface IncrByFloatCommand : RedisCommandSpec<Double> {
    suspend fun encode(
        @RedisKey key: String,
        increment: Double
    ): CommandRequest<String>
}
