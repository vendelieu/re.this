package eu.vendeli.rethis.api.spec.commands.hash

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.request.common.UpdateStrategyOption
import eu.vendeli.rethis.shared.types.*
import kotlin.time.Duration

@RedisCommand(
    "HEXPIRE",
    RedisOperation.WRITE,
    [RespCode.ARRAY, RespCode.SIMPLE_ERROR],
)
fun interface HExpireCommand : RedisCommandSpec<List<Long>> {
    suspend fun encode(
        key: String,
        seconds: @RedisMeta.OutgoingTimeUnit(TimeUnit.SECONDS) Duration,
        @RedisOption.Token("FIELDS") @RedisMeta.WithSizeParam("numfields") vararg field: String,
        condition: UpdateStrategyOption?,
    ): CommandRequest
}
