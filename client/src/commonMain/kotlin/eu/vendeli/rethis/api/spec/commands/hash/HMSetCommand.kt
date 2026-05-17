package eu.vendeli.rethis.api.spec.commands.hash

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.common.FieldValue
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@Deprecated(
    message = "HMSET is deprecated as of Redis 4.0.0. Use HSET (which became variadic in 4.0.0) instead.",
)
@RedisCommand("HMSET", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface HMSetCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        key: String,
        vararg data: FieldValue,
    ): CommandRequest
}
