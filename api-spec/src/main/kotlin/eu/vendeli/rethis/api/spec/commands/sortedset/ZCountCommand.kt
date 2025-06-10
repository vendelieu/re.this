package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("ZCOUNT", RedisOperation.READ, [RespCode.INTEGER])
fun interface ZCountCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        min: Double,
        max: Double
    ): CommandRequest
}
