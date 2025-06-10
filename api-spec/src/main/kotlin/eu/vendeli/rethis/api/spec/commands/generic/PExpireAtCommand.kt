package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.common.UpdateStrategyOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import kotlinx.datetime.Instant

@RedisCommand("PEXPIREAT", RedisOperation.WRITE, [RespCode.INTEGER], extensions = [UpdateStrategyOption::class])
fun interface PExpireAtCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        key: String,
        unixTimeMilliseconds: Instant,
        @RedisOptional condition: UpdateStrategyOption?,
    ): CommandRequest
}
