package eu.vendeli.rethis.api.spec.commands.hash

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("HSETNX", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface HSetNxCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        @RedisKey key: String,
        field: String,
        value: String,
    ): CommandRequest<String>
}
