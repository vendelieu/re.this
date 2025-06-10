package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.stream.XPendingMainFilter
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand(
    "XPENDING",
    RedisOperation.READ,
    [RespCode.ARRAY],
    extensions = [XPendingMainFilter::class],
)
fun interface XPendingCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(
        key: String,
        group: String,
        @RedisOptional @RedisOption.Token("IDLE") minIdleTime: Long?,
        @RedisOptional filter: XPendingMainFilter?,
        @RedisOptional consumer: String?,
    ): CommandRequest
}
