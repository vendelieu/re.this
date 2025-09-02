package eu.vendeli.rethis.api.spec.commands.connection

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.connection.ClientKillOptions
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("CLIENT KILL", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface ClientKillCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        vararg filter: ClientKillOptions,
    ): CommandRequest
}
