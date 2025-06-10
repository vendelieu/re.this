package eu.vendeli.rethis.api.spec.commands.set

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("SISMEMBER", RedisOperation.READ, [RespCode.INTEGER])
fun interface SIsMemberCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        key: String,
        member: String,
    ): CommandRequest
}
