package eu.vendeli.rethis.api.spec.commands.connection

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("CLIENT ID", RedisOperation.READ, [RespCode.INTEGER])
fun interface ClientIdCommand : RedisCommandSpec<Long> {
    suspend fun encode(): CommandRequest
}
