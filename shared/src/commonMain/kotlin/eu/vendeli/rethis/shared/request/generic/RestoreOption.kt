package eu.vendeli.rethis.shared.request.generic

import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.types.TimeUnit
import kotlin.time.Duration

sealed class RestoreOption {
    data object REPLACE : RestoreOption()

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
