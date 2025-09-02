package eu.vendeli.rethis.api.spec.commands.string

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("DECRBY", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface DecrByCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        decrement: Long
    ): CommandRequest
}
