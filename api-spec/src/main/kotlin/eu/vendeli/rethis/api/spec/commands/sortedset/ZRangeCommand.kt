package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.request.sortedset.ZRangeOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

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
        @RedisOption.Token("WITHSCORES") withScores: Boolean?,
    ): CommandRequest
}
