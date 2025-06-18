package eu.vendeli.rethis.api.spec.commands.connection

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.request.connection.ClientPauseMode
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("CLIENT PAUSE", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface ClientPauseCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        timeout: Long,
        mode: ClientPauseMode?
    ): CommandRequest
}
