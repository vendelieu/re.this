package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("ACL DRYRUN", RedisOperation.READ, [RespCode.BULK, RespCode.SIMPLE_STRING])
fun interface AclDryRunCommand : RedisCommandSpec<String> {
    suspend fun encode(
        username: String,
        command: String,
        vararg arg: String,
    ): CommandRequest
}
