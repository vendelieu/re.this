package eu.vendeli.rethis.api.spec.commands.connection

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.connection.HelloAuth
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("HELLO", RedisOperation.READ, [RespCode.MAP, RespCode.SIMPLE_ERROR], extensions = [HelloAuth::class])
fun interface HelloCommand : RedisCommandSpec<Map<String, RType?>> {
    suspend fun encode(
        @RedisOptional protover: Long?,
        @RedisOptional auth: HelloAuth?,
        @RedisOptional @RedisOption.Token("SETNAME") clientname: String?,
    ): CommandRequest
}
