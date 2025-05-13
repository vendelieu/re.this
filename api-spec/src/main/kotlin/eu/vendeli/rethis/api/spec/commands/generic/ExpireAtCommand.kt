package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.UpdateStrategyOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import kotlinx.datetime.Instant

@RedisCommand("EXPIREAT", RedisOperation.WRITE, [RespCode.INTEGER], extensions = [UpdateStrategyOption::class])
fun interface ExpireAtCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        @RedisKey key: String,
        unixTimeSeconds: Instant,
        @RedisOptional option: UpdateStrategyOption?
    ): CommandRequest<String>
}
