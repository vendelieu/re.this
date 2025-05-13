package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.ZPopCommonOption
import eu.vendeli.rethis.api.spec.common.response.MPopResult
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand(
    "BZMPOP",
    RedisOperation.READ,
    [RespCode.ARRAY, RespCode.NULL],
    isBlocking = true,
    extensions = [ZPopCommonOption::class],
)
fun interface BzMPopCommand : RedisCommandSpec<List<MPopResult>> {
    suspend fun encode(
        timeout: Double,
        minMax: ZPopCommonOption,
        @RedisKey @RedisMeta.WithSizeParam("numkeys") vararg key: String,
        @RedisOptional count: Long?,
    ): CommandRequest<List<String>>
}
