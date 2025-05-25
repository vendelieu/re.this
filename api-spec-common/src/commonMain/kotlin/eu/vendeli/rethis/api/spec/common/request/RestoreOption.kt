package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import kotlin.time.Duration

sealed class RestoreOption {
    @RedisOption
    data object REPLACE : RestoreOption()

    @RedisOption
    data object ABSTTL : RestoreOption()

    @RedisOption.Token("IDLETIME")
    class IdleTime(
        val seconds: Duration,
    ) : RestoreOption()

    @RedisOption.Token("FREQ")
    class Frequency(
        val frequency: Long,
    ) : RestoreOption()
}
