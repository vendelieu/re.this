package eu.vendeli.rethis.api.spec.commands.connection

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@Deprecated(
    message = "QUIT is deprecated as of Redis 7.2.0. " +
        "Use RESET to reset connection state, or close the underlying socket.",
)
@RedisCommand("QUIT", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface QuitCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(): CommandRequest
}
