package eu.vendeli.rethis.api.spec.common.request.string

import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.types.TimeUnit
import kotlin.time.Duration
import kotlin.time.Instant

sealed class SetOption


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

    data object KEEPTTL : SetExpire()
}


sealed class UpsertMode : SetOption() {
    data object NX : UpsertMode()
    data object XX : UpsertMode()
}

data object GET : SetOption()
