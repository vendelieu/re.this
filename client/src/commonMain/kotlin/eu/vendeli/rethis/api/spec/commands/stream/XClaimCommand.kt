package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.shared.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.request.stream.XClaimOption
import eu.vendeli.rethis.shared.types.*

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
        @RIgnoreSpecAbsence idle: XClaimOption.Idle?,
        @RIgnoreSpecAbsence time: XClaimOption.Time?,
        @RIgnoreSpecAbsence retryCount: XClaimOption.RetryCount?,
        @RedisOption.Token("FORCE") force: Boolean?,
        @RedisOption.Token("JUSTID") @RedisOption.Name("justid") justId: Boolean?,
        @RedisOption.Name("lastid") lastId: XClaimOption.LastId?,
    ): CommandRequest
}
