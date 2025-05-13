package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import kotlin.time.Duration

sealed class GetExOption {
    @RedisOption
    class EX(
        seconds: Duration,
    ) : GetExOption()

    @RedisOption
    class PX(
        milliseconds: Duration,
    ) : GetExOption()

    @RedisOption
    class EXAT(
        seconds: Duration,
    ) : GetExOption()

    @RedisOption
    class PXAT(
        milliseconds: Duration,
    ) : GetExOption()

    @RedisOption.Name("PERSIST")
    data object Persist : GetExOption()
}
