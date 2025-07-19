package eu.vendeli.rethis.api.spec.common.request.server

import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta.OutgoingTimeUnit
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.types.TimeUnit
import kotlin.time.Duration

sealed class FailoverOptions {
    @RedisOption.Token("TO")
    class To(
        val host: String,
        val port: Long,
        @RedisOption.Token("FORCE") val force: Boolean = false,
    ) : FailoverOptions()

    data object ABORT : FailoverOptions()

    @RedisOption.Token("TIMEOUT")
    class Timeout(
        val milliseconds: @OutgoingTimeUnit(TimeUnit.MILLISECONDS) Duration,
    ) : FailoverOptions()
}
