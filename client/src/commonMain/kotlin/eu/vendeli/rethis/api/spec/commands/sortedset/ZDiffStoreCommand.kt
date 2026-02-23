package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("ZDIFFSTORE", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface ZDiffStoreCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        destination: String,
        @RedisMeta.WithSizeParam("numkeys") vararg key: String
    ): CommandRequest
}
