package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

sealed class XClaimOption {
    @RedisOption.Name("IDLE")
    class Idle(
        val ms: Long,
    ) : XClaimOption()

    @RedisOption.Name("TIME")
    class Time(
        val unixTimeMillis: Long,
    ) : XClaimOption()

    @RedisOption.Name("RETRYCOUNT")
    class RetryCount(
        val count: Long,
    ) : XClaimOption()

    @RedisOption.Name("LASTID")
    class LastId(
        val id: String,
    ) : XClaimOption()
}
