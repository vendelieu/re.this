package eu.vendeli.rethis.api.spec.commands.set

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("SISMEMBER", RedisOperation.READ, [RespCode.INTEGER])
fun interface SIsMemberCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        key: String,
        member: String,
    ): CommandRequest
}
