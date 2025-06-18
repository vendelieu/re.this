package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.request.common.UpdateStrategyOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("EXPIRE", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface ExpireCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        key: String,
        seconds: Long,
        condition: UpdateStrategyOption?
    ): CommandRequest
}
