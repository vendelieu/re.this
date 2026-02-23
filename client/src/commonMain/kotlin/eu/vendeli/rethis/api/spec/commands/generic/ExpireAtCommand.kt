package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.request.common.UpdateStrategyOption
import eu.vendeli.rethis.shared.types.*
import kotlin.time.Instant

@RedisCommand("EXPIREAT", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface ExpireAtCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        key: String,
        unixTimeSeconds: @RedisMeta.OutgoingTimeUnit(TimeUnit.SECONDS) Instant,
        condition: UpdateStrategyOption?,
    ): CommandRequest
}
