package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("ACL USERS", RedisOperation.READ, [RespCode.ARRAY])
fun interface AclUsersCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(): CommandRequest
}
