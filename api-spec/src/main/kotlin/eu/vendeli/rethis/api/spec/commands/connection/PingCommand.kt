package eu.vendeli.rethis.api.spec.commands.connection

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("PING", RedisOperation.READ, [RespCode.SIMPLE_STRING, RespCode.BULK])
fun interface PingCommand : RedisCommandSpec<String> {
    suspend fun encode(
        @RedisOptional message: String?
    ): CommandRequest
}
