package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import kotlin.time.Duration

sealed class RestoreOption {
    @RedisOption
    data object REPLACE : RestoreOption()

    @RedisOption
    data object ABSTTL : RestoreOption()

    @RedisOption
    class IDLETIME(
        val seconds: Duration,
    ) : RestoreOption()

    @RedisOption
    class FREQ(
        val frequency: Long,
    ) : RestoreOption()
}
