package eu.vendeli.rethis.api.spec.commands.hash

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
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
    extensions = [UpdateStrategyOption::class],
)
fun interface HExpireCommand : RedisCommandSpec<List<Long>> {
    suspend fun encode(
        @RedisKey key: String,
        seconds: Duration,
        @RedisMeta.WithSizeParam("numfields") vararg field: String,
        @RedisOptional condition: UpdateStrategyOption?,
    ): CommandRequest<String>
}
