package eu.vendeli.rethis.api.spec.commands.list

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.list.LPosOption
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand(
    "LPOS",
    RedisOperation.READ,
    [RespCode.ARRAY],
    extensions = [LPosOption.Count::class, LPosOption.CommonOption::class],
)
fun interface LPosCountCommand : RedisCommandSpec<List<Long>> {
    suspend fun encode(
        @RedisKey key: String,
        element: String,
        @RedisMeta.IgnoreCheck([ValidityCheck.OPTIONALITY]) count: LPosOption.Count,
        @RedisOptional vararg option: LPosOption.CommonOption,
    ): CommandRequest<String>
}
