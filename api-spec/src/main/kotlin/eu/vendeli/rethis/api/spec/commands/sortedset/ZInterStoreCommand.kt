package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.request.sortedset.ZAggregate
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("ZINTERSTORE", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface ZInterStoreCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        destination: String,
        @RedisMeta.WithSizeParam("numkeys") vararg key: String,
        @RedisOption.Token("WEIGHTS") weight: List<Long>?,
        aggregate: ZAggregate?,
    ): CommandRequest
}
