package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("ZRANK", RedisOperation.READ, [RespCode.ARRAY, RespCode.NULL])
fun interface ZRankWithScoresCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(
        key: String,
        member: String,
        @RedisOption.Token("WITHSCORE") @RedisOption.Name("withscore") withScore: Boolean?,
    ): CommandRequest
}
