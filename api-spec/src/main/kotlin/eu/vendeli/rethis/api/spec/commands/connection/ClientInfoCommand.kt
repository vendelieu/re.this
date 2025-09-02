package eu.vendeli.rethis.api.spec.commands.connection

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("CLIENT INFO", RedisOperation.READ, [RespCode.BULK])
fun interface ClientInfoCommand : RedisCommandSpec<String> {
    suspend fun encode(): CommandRequest
}
