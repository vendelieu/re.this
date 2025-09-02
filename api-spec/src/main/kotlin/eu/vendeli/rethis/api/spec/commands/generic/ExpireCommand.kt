package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.request.common.UpdateStrategyOption
import eu.vendeli.rethis.shared.types.*
import kotlin.time.Duration

@RedisCommand("EXPIRE", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface ExpireCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        key: String,
        seconds: @RedisMeta.OutgoingTimeUnit(TimeUnit.SECONDS) Duration,
        condition: UpdateStrategyOption?
    ): CommandRequest
}
