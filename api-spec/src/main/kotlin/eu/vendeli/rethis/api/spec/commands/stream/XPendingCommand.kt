package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.stream.XPendingMainFilter
import eu.vendeli.rethis.shared.types.*

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
