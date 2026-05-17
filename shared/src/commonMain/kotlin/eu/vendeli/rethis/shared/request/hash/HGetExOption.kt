package eu.vendeli.rethis.shared.request.hash

import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.types.TimeUnit
import kotlin.time.Duration
import kotlin.time.Instant

sealed class HGetExOption {
    @RedisOption.Token("EX")
    class Ex(val seconds: @RedisMeta.OutgoingTimeUnit(TimeUnit.SECONDS) Duration) : HGetExOption()

    @RedisOption.Token("PX")
    class Px(val milliseconds: @RedisMeta.OutgoingTimeUnit(TimeUnit.MILLISECONDS) Duration) : HGetExOption()

    @RedisOption.Token("EXAT")
    class ExAt(val unixTimeSeconds: @RedisMeta.OutgoingTimeUnit(TimeUnit.SECONDS) Instant) : HGetExOption()

    @RedisOption.Token("PXAT")
    class PxAt(val unixTimeMilliseconds: @RedisMeta.OutgoingTimeUnit(TimeUnit.MILLISECONDS) Instant) : HGetExOption()

    data object PERSIST : HGetExOption()
}
