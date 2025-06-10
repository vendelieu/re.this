package eu.vendeli.rethis.api.spec.commands.set

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("SMOVE", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface SMoveCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        source: String,
        destination: String,
        member: String,
    ): CommandRequest
}
