package eu.vendeli.rethis.api.spec.common.request.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer
import kotlinx.datetime.Instant

@RedisOptionContainer
sealed class XClaimOption {
    @RedisOption.Token("IDLE")
    class Idle(
        val ms: Long,
    ) : XClaimOption()

    @RedisOption.Token("TIME")
    class Time(
        val unixTimeMilliseconds: Instant,
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
