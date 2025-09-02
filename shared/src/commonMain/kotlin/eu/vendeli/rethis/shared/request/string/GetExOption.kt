package eu.vendeli.rethis.shared.request.string

import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.types.TimeUnit
import kotlin.time.Duration
import kotlin.time.Instant


sealed class GetExOption {
    @RedisOption.Token("EX")
    class Ex(
        val seconds: @RedisMeta.OutgoingTimeUnit(TimeUnit.SECONDS) Duration,
    ) : GetExOption()

    @RedisOption.Token("PX")
    class Px(
        val milliseconds: @RedisMeta.OutgoingTimeUnit(TimeUnit.MILLISECONDS) Duration,
    ) : GetExOption()

    @RedisOption.Token("EXAT")
    class ExAt(
        val unixTimeSeconds: @RedisMeta.OutgoingTimeUnit(TimeUnit.SECONDS) Instant,
    ) : GetExOption()

    @RedisOption.Token("PXAT")
    class PxAt(
        val unixTimeMilliseconds: @RedisMeta.OutgoingTimeUnit(TimeUnit.MILLISECONDS) Instant,
    ) : GetExOption()

    @RedisOption.Token("PERSIST")
    data object Persist : GetExOption()
}
