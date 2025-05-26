package eu.vendeli.rethis.api.spec.common.request.string

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer
import kotlinx.datetime.Instant
import kotlin.time.Duration

sealed class SetOption

@RedisOptionContainer
sealed class SetExpire : SetOption() {
    @RedisOption.Token("EX")
    class Ex(
        val seconds: Duration,
    ) : SetExpire()

    @RedisOption.Token("PX")
    class Px(
        val milliseconds: Duration,
    ) : SetExpire()

    @RedisOption.Token("EXAT")
    class ExAt(
        val unixTimeSeconds: Instant,
    ) : SetExpire()

    @RedisOption.Token("PXAT")
    class PxAt(
        val unixTimeMilliseconds: Instant,
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
