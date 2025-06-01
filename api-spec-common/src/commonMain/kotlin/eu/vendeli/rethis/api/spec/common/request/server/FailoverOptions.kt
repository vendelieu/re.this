package eu.vendeli.rethis.api.spec.common.request.server

import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta.IgnoreCheck
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta.OutgoingTimeUnit
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.types.TimeUnit
import eu.vendeli.rethis.api.spec.common.types.ValidityCheck
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
        @IgnoreCheck([ValidityCheck.TYPE]) val milliseconds: @OutgoingTimeUnit(TimeUnit.MILLISECONDS) Duration,
    ) : FailoverOptions()
}
