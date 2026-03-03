package eu.vendeli.rethis.api.spec.commands.set

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("SINTERCARD", RedisOperation.READ, [RespCode.INTEGER])
fun interface SInterCardCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        @RedisMeta.WithSizeParam("numkeys") vararg key: String,
        @RedisOption.Token("LIMIT") limit: Long?,
    ): CommandRequest
}
