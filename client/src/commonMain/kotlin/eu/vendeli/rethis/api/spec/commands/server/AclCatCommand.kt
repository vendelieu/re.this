package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("ACL CAT", RedisOperation.READ, [RespCode.ARRAY, RespCode.SIMPLE_ERROR])
fun interface AclCatCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(category: String?): CommandRequest
}
