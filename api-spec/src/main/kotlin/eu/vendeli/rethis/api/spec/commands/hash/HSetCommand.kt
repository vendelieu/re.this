package eu.vendeli.rethis.api.spec.commands.hash

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("HSET", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface HSetCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        @RedisKey key: String,
        vararg fieldValue: Pair<String, String>
    ): CommandRequest<String>
}
