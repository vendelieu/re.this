package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("MOVE", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface MoveCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        key: String,
        db: Long
    ): CommandRequest
}
