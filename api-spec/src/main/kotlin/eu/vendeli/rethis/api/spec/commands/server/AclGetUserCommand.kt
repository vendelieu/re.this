package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("ACL GETUSER", RedisOperation.READ, [RespCode.ARRAY, RespCode.MAP, RespCode.NULL])
fun interface AclGetUserCommand : RedisCommandSpec<Map<String, RType?>> {
    suspend fun encode(username: String): CommandRequest<Nothing>
}
