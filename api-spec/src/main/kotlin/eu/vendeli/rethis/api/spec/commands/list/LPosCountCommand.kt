package eu.vendeli.rethis.api.spec.commands.list

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.request.LPosOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

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
        count: LPosOption.Count,
        vararg option: LPosOption.CommonOption,
    ): CommandRequest<String>
}
