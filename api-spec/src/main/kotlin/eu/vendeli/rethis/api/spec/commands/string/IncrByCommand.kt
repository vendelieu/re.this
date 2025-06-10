package eu.vendeli.rethis.api.spec.commands.string

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("INCRBY", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface IncrByCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        increment: Long
    ): CommandRequest
}
