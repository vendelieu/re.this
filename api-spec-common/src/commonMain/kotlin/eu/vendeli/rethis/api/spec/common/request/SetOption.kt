package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer
import kotlinx.datetime.Instant
import kotlin.time.Duration

sealed class SetOption

@RedisOptionContainer
sealed class SetExpire : SetOption() {
    @RedisOption
    class EX(
        seconds: Duration,
    ) : SetExpire()

    @RedisOption
    class PX(
        milliseconds: Duration,
    ) : SetExpire()

    @RedisOption
    class EXAT(
        instant: Instant,
    ) : SetExpire()

    @RedisOption
    class PXAT(
        instant: Instant,
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
