package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("ACL SETUSER", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface AclSetUserCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(username: String, vararg rule: String): CommandRequest
}
