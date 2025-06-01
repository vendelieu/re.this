package eu.vendeli.rethis.api.spec.common.request.string

import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer
import eu.vendeli.rethis.api.spec.common.types.TimeUnit
import kotlinx.datetime.Instant
import kotlin.time.Duration

sealed class SetOption

@RedisOptionContainer
sealed class SetExpire : SetOption() {
    @RedisOption.Token("EX")
    class Ex(
        val seconds: @RedisMeta.OutgoingTimeUnit(TimeUnit.SECONDS) Duration,
    ) : SetExpire()

    @RedisOption.Token("PX")
    class Px(
        val milliseconds: @RedisMeta.OutgoingTimeUnit(TimeUnit.MILLISECONDS) Duration,
    ) : SetExpire()

    @RedisOption.Token("EXAT")
    class ExAt(
        val unixTimeSeconds: @RedisMeta.OutgoingTimeUnit(TimeUnit.SECONDS) Instant,
    ) : SetExpire()

    @RedisOption.Token("PXAT")
    class PxAt(
        val unixTimeMilliseconds: @RedisMeta.OutgoingTimeUnit(TimeUnit.MILLISECONDS) Instant,
    ) : SetExpire()

    @RedisOption
    data object KEEPTTL : SetExpire()
}

@RedisOptionContainer
sealed class UpsertMode : SetOption() {
    @RedisOption
    data object NX : UpsertMode()

    @RedisOption
    data object XX : UpsertMode()
}

@RedisOption
data object GET : SetOption()
