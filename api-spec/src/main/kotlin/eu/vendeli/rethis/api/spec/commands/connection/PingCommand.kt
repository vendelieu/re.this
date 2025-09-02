package eu.vendeli.rethis.api.spec.commands.connection

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("PING", RedisOperation.READ, [RespCode.SIMPLE_STRING, RespCode.BULK])
fun interface PingCommand : RedisCommandSpec<String> {
    suspend fun encode(
        message: String?
    ): CommandRequest
}
