package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("ACL DELUSER", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface AclDelUserCommand : RedisCommandSpec<Long> {
    suspend fun encode(vararg username: String): CommandRequest
}
