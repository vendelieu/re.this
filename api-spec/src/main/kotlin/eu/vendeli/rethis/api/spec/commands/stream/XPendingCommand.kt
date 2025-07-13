package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.request.stream.XPendingMainFilter
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand(
    "XPENDING",
    RedisOperation.READ,
    [RespCode.ARRAY],
)
fun interface XPendingCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(
        key: String,
        group: String,
        filters: XPendingMainFilter?,
    ): CommandRequest
}
