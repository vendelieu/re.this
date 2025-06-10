package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.sortedset.ZAggregate
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("ZINTERSTORE", RedisOperation.WRITE, [RespCode.INTEGER], extensions = [ZAggregate::class])
fun interface ZInterStoreCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        destination: String,
        @RedisMeta.WithSizeParam("numkeys") vararg key: String,
        @RedisOptional @RedisOption.Token("WEIGHTS") weight: List<Long>,
        @RedisOptional aggregate: ZAggregate?,
    ): CommandRequest
}
