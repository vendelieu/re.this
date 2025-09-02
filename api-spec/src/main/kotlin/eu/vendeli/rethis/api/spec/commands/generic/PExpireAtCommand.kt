package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.request.common.UpdateStrategyOption
import eu.vendeli.rethis.shared.types.*
import kotlin.time.Instant

@RedisCommand("PEXPIREAT", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface PExpireAtCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        key: String,
        unixTimeMilliseconds: @RedisMeta.OutgoingTimeUnit(TimeUnit.MILLISECONDS) Instant,
        condition: UpdateStrategyOption?,
    ): CommandRequest
}
