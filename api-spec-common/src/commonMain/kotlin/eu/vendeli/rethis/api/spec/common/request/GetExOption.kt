package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import kotlin.time.Duration

sealed class GetExOption {
    @RedisOption.Token("EX")
    class EX(
        seconds: Duration,
    ) : GetExOption()

    @RedisOption.Token("PX")
    class PX(
        milliseconds: Duration,
    ) : GetExOption()

    @RedisOption.Token("EXAT")
    class EXAT(
        seconds: Duration,
    ) : GetExOption()

    @RedisOption.Token("PXAT")
    class PXAT(
        milliseconds: Duration,
    ) : GetExOption()

    @RedisOption.Token("PERSIST")
    data object Persist : GetExOption()
}
