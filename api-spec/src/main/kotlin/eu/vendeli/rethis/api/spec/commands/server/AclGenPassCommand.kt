package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("ACL GENPASS", RedisOperation.READ, [RespCode.BULK])
fun interface AclGenPassCommand : RedisCommandSpec<String> {
    suspend fun encode(bits: Long?): CommandRequest
}
