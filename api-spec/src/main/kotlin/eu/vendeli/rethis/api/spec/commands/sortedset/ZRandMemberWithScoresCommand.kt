package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("ZRANDMEMBER", RedisOperation.READ, [RespCode.ARRAY])
fun interface ZRandMemberWithScoresCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(
        key: String,
        count: Long,
        @RedisOption.Token("WITHSCORES") @RedisOption.Name("withscores") withScores: Boolean?
    ): CommandRequest
}
