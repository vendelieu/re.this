package eu.vendeli.rethis.api.spec.commands.string

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("DECRBY", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface DecrByCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        @RedisKey key: String,
        decrement: Long
    ): CommandRequest<String>
}
