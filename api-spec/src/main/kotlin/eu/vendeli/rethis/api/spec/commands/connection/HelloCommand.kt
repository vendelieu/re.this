package eu.vendeli.rethis.api.spec.commands.connection

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("HELLO", RedisOperation.READ, [RespCode.MAP, RespCode.SIMPLE_ERROR])
fun interface HelloCommand : RedisCommandSpec<Map<String, RType?>> {
    suspend fun encode(
        @RedisOptional proto: Long?,
        @RedisOptional username: String?,
        @RedisOptional password: String?,
        @RedisOptional name: String?
    ): CommandRequest<Nothing>
}
