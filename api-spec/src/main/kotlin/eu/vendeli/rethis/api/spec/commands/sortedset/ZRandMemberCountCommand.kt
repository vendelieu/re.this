package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("ZRANDMEMBER", RedisOperation.READ, [RespCode.ARRAY])
fun interface ZRandMemberCountCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(
        key: String,
        count: Long,
        @RedisOption.Token("WITHSCORES") @RedisOption.Name("withscores") withScores: Boolean?,
    ): CommandRequest
}
