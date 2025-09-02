package eu.vendeli.rethis.api.spec.commands.connection

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("CLIENT KILL", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface ClientKillStringCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        @RedisOption.Name("oldFormat") ipPort: String,
    ): CommandRequest
}
