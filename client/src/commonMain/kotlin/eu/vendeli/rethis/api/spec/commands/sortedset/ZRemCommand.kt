package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("ZREM", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface ZRemCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        vararg member: String
    ): CommandRequest
}
