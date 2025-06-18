package eu.vendeli.rethis.api.spec.commands.hash

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.request.common.UpdateStrategyOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import kotlin.time.Duration

@RedisCommand(
    "HEXPIRE",
    RedisOperation.WRITE,
    [RespCode.ARRAY, RespCode.SIMPLE_ERROR],
)
fun interface HExpireCommand : RedisCommandSpec<List<Long>> {
    suspend fun encode(
        key: String,
        seconds: Duration,
        @RedisMeta.WithSizeParam("numfields") vararg field: String,
        condition: UpdateStrategyOption?,
    ): CommandRequest
}
