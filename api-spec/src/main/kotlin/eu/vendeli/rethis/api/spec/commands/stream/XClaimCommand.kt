package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.stream.XClaimOption
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand(
    "XCLAIM",
    RedisOperation.WRITE,
    [RespCode.ARRAY],
    extensions = [XClaimOption.Idle::class, XClaimOption.Time::class, XClaimOption.RetryCount::class, XClaimOption.LastId::class],
)
fun interface XClaimCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(
        @RedisKey key: String,
        group: String,
        consumer: String,
        minIdleTime: String,
        vararg id: String,
        @RedisOptional idle: XClaimOption.Idle?,
        @RedisOptional time: XClaimOption.Time?,
        @RedisOptional retryCount: XClaimOption.RetryCount?,
        @RedisOptional @RedisOption.Token("FORCE") force: Boolean?,
        @RedisOptional @RedisOption.Token("JUSTID") justId: Boolean?,
        @RedisOptional lastId: XClaimOption.LastId?,
    ): CommandRequest<String>
}
