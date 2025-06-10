package eu.vendeli.rethis.api.spec.commands.set

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("SADD", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface SAddCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        vararg member: String,
    ): CommandRequest
}
