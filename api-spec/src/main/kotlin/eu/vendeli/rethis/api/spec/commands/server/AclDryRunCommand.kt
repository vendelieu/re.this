package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("ACL DRYRUN", RedisOperation.READ, [RespCode.BULK, RespCode.SIMPLE_STRING])
fun interface AclDryRunCommand : RedisCommandSpec<String> {
    suspend fun encode(
        username: String,
        command: String,
        vararg arg: String,
    ): CommandRequest
}
