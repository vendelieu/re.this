package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.request.sortedset.ZAggregate
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("ZUNION", RedisOperation.READ, [RespCode.ARRAY])
fun interface ZUnionCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(
        @RedisMeta.WithSizeParam("numkeys") vararg key: String,
        @RedisOption.Token("WEIGHTS") weight: List<Long>?,
        aggregate: ZAggregate?,
        @RedisOption.Token("WITHSCORES") withScores: Boolean?,
    ): CommandRequest
}
