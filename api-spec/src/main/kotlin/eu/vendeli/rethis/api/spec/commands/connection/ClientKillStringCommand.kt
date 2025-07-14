package eu.vendeli.rethis.api.spec.commands.connection

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("CLIENT KILL", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface ClientKillStringCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        @RedisOption.Name("oldFormat") ipPort: String,
    ): CommandRequest
}
