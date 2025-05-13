package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("ZREMRANGEBYSCORE", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface ZRemRangeByScoreCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        @RedisKey key: String,
        min: Double,
        max: Double
    ): CommandRequest<String>
}
