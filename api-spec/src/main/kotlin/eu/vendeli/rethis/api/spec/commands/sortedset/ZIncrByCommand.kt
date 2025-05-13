package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("ZINCRBY", RedisOperation.WRITE, [RespCode.BULK])
fun interface ZIncrByCommand : RedisCommandSpec<Double> {
    suspend fun encode(
        @RedisKey key: String,
        member: String,
        increment: Long
    ): CommandRequest<String>
}
