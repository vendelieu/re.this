package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("ZREVRANK", RedisOperation.READ, [RespCode.ARRAY, RespCode.NULL])
fun interface ZRevRankWithScoreCommand : RedisCommandSpec<List<Long>> {
    suspend fun encode(
        key: String,
        member: String,
        @RedisOption.Token("WITHSCORE") @RedisOption.Name("withscore") withScore: Boolean,
    ): CommandRequest
}
