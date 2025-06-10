package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.response.ZPopResult
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand(
    "BZPOPMIN",
    RedisOperation.WRITE,
    [RespCode.ARRAY, RespCode.NULL],
    isBlocking = true,
)
fun interface BzPopMinCommand : RedisCommandSpec<ZPopResult> {
    suspend fun encode(
        timeout: Double,
        vararg key: String,
    ): CommandRequest
}
