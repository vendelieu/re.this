package eu.vendeli.rethis.api.spec.commands.hash

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.request.common.UpdateStrategyOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.TimeUnit
import kotlin.time.Duration

@RedisCommand("HPEXPIRE", RedisOperation.WRITE, [RespCode.ARRAY, RespCode.SIMPLE_ERROR])
fun interface HPExpireCommand : RedisCommandSpec<List<Long>> {
    suspend fun encode(
        key: String,
        milliseconds:
            @RedisMeta.OutgoingTimeUnit(TimeUnit.MILLISECONDS)
            Duration,
        condition: UpdateStrategyOption?,
        @RedisOption.Token("FIELDS") @RedisMeta.WithSizeParam("numfields") vararg field: String,
    ): CommandRequest
}
