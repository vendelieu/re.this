package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.request.common.UpdateStrategyOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import kotlin.time.Instant

@RedisCommand("EXPIREAT", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface ExpireAtCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        key: String,
        unixTimeSeconds: Instant,
        condition: UpdateStrategyOption?,
    ): CommandRequest
}
