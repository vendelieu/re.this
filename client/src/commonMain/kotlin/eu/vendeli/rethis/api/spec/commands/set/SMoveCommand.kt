package eu.vendeli.rethis.api.spec.commands.set

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("SMOVE", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface SMoveCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        source: String,
        destination: String,
        member: String,
    ): CommandRequest
}
