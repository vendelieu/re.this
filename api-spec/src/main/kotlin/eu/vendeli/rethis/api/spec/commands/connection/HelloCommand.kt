package eu.vendeli.rethis.api.spec.commands.connection

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.request.connection.HelloAuth
import eu.vendeli.rethis.shared.types.*

@RedisCommand("HELLO", RedisOperation.READ, [RespCode.MAP, RespCode.ARRAY, RespCode.SIMPLE_ERROR])
fun interface HelloCommand : RedisCommandSpec<Map<String, RType>> {
    suspend fun encode(
        protover: Long?,
        auth: HelloAuth?,
        @RedisOption.Token("SETNAME") clientname: String?,
    ): CommandRequest
}
