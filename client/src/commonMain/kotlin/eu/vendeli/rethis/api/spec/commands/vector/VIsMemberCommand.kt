package eu.vendeli.rethis.api.spec.commands.vector

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("VISMEMBER", RedisOperation.READ, [RespCode.INTEGER, RespCode.BOOLEAN])
fun interface VIsMemberCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(key: String, element: String): CommandRequest
}
