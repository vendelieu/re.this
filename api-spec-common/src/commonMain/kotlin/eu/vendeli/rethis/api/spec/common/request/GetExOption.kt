package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer
import kotlinx.datetime.Instant
import kotlin.time.Duration

@RedisOptionContainer
sealed class GetExOption {
    @RedisOption.Token("EX")
    class Ex(
        val seconds: Duration,
    ) : GetExOption()

    @RedisOption.Token("PX")
    class Px(
        val milliseconds: Duration,
    ) : GetExOption()

    @RedisOption.Token("EXAT")
    class ExAt(
        val unixTimeSeconds: Instant,
    ) : GetExOption()

    @RedisOption.Token("PXAT")
    class PxAt(
        val unixTimeMilliseconds: Instant,
    ) : GetExOption()

    @RedisOption.Token("PERSIST")
    data object Persist : GetExOption()
}
