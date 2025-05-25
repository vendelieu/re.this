package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("ZRANDMEMBER", RedisOperation.READ, [RespCode.ARRAY])
fun interface ZRandMemberCountCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(
        @RedisKey key: String,
        count: Long,
        @RedisOptional @RedisOption.Token("WITHSCORES") withScores: Boolean?,
    ): CommandRequest<String>
}
