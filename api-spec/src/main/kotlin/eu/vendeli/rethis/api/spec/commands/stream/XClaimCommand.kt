package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.request.stream.XClaimOption
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand(
    "XCLAIM",
    RedisOperation.WRITE,
    [RespCode.ARRAY],
)
fun interface XClaimCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(
        key: String,
        group: String,
        consumer: String,
        minIdleTime: String,
        vararg id: String,
        idle: XClaimOption.Idle?,
        time: XClaimOption.Time?,
        retryCount: XClaimOption.RetryCount?,
        @RedisOption.Token("FORCE") force: Boolean?,
        @RedisOption.Token("JUSTID") justId: Boolean?,
        lastId: XClaimOption.LastId?,
    ): CommandRequest
}
