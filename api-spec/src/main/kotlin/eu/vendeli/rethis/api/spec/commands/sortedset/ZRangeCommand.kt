package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.request.sortedset.ZRangeOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand(
    "ZRANGE",
    RedisOperation.READ,
    [RespCode.ARRAY],
)
fun interface ZRangeCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(
        key: String,
        start: String,
        stop: String,
        @RedisOption.Name("sortby") sortBy: ZRangeOption.Type?,
        @RedisOption.Token("REV") rev: Boolean?,
        limit: ZRangeOption.Limit?,
        @RedisOption.Token("WITHSCORES") @RedisOption.Name("withscores") withScores: Boolean?,
    ): CommandRequest
}
