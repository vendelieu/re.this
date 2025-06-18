package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.request.common.UpdateStrategyOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("PEXPIRE", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface PExpireCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        key: String,
        milliseconds: Long,
        condition: UpdateStrategyOption?
    ): CommandRequest
}
