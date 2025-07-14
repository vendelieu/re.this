package eu.vendeli.rethis.api.spec.commands.connection

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.request.connection.ClientKillOptions
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("CLIENT KILL", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface ClientKillCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        vararg filter: ClientKillOptions,
    ): CommandRequest
}
