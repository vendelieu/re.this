package eu.vendeli.rethis.api.spec.common.request.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.types.TimeUnit
import kotlin.time.Instant

sealed class XClaimOption {
    @RedisOption.Token("IDLE")
    class Idle(
        val ms: Long,
    ) : XClaimOption()

    @RedisOption.Token("TIME")
    class Time(
        val unixTimeMilliseconds: @RedisMeta.OutgoingTimeUnit(TimeUnit.MILLISECONDS) Instant,
    ) : XClaimOption()

    @RedisOption.Token("RETRYCOUNT")
    class RetryCount(
        val count: Long,
    ) : XClaimOption()

    @RedisOption.Token("LASTID")
    class LastId(
        val lastid: String,
    ) : XClaimOption()
}
