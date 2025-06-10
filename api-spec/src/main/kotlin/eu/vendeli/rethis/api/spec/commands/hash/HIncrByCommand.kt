package eu.vendeli.rethis.api.spec.commands.hash

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("HINCRBY", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface HIncrByCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        field: String,
        increment: Long
    ): CommandRequest
}
