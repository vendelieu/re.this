package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.request.sortedset.ZAggregate
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("ZINTER", RedisOperation.READ, [RespCode.ARRAY])
fun interface ZInterCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(
        @RedisMeta.WithSizeParam("numkeys") vararg key: String,
        @RedisOption.Token("WEIGHTS") weight: List<Long>?,
        aggregate: ZAggregate?,
        @RedisOption.Token("WITHSCORES") @RedisOption.Name("withscores") withScores: Boolean?
    ): CommandRequest
}
