package eu.vendeli.rethis.api.spec.common.request.server

import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
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
        @RedisMeta.IgnoreCheck([ValidityCheck.TYPE]) val milliseconds: Duration,
    ) : FailoverOptions()
}
