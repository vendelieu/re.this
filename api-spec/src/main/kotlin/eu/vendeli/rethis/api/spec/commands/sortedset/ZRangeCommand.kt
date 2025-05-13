package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.ZRangeOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("ZRANGE", RedisOperation.READ, [RespCode.ARRAY], extensions = [ZRangeOption.Type::class, ZRangeOption.LIMIT::class])
fun interface ZRangeCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(
        @RedisKey key: String,
        start: String,
        stop: String,
        @RedisOptional type: ZRangeOption.Type?,
        @RedisOptional rev: Boolean?,
        @RedisOptional limit: ZRangeOption.LIMIT?,
        @RedisOptional withScores: Boolean?
    ): CommandRequest<String>
}
