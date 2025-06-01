package eu.vendeli.rethis.api.spec.common.request.string

import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer
import eu.vendeli.rethis.api.spec.common.types.TimeUnit
import kotlinx.datetime.Instant
import kotlin.time.Duration

@RedisOptionContainer
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
