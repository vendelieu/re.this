package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("ZINCRBY", RedisOperation.WRITE, [RespCode.BULK, RespCode.DOUBLE])
fun interface ZIncrByCommand : RedisCommandSpec<Double> {
    suspend fun encode(
        key: String,
        member: String,
        increment: Long
    ): CommandRequest
}
