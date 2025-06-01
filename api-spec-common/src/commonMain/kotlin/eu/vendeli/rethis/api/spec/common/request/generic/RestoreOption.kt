package eu.vendeli.rethis.api.spec.common.request.generic

import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.types.TimeUnit
import kotlin.time.Duration

sealed class RestoreOption {
    @RedisOption
    data object REPLACE : RestoreOption()

    @RedisOption
    data object ABSTTL : RestoreOption()

    @RedisOption.Token("IDLETIME")
    class IdleTime(
        val seconds: @RedisMeta.OutgoingTimeUnit(TimeUnit.SECONDS) Duration,
    ) : RestoreOption()

    @RedisOption.Token("FREQ")
    class Frequency(
        val frequency: Long,
    ) : RestoreOption()
}
