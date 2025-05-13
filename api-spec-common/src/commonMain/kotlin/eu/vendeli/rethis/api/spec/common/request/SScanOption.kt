package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

sealed class SScanOption {
    @RedisOption
    class MATCH(
        val pattern: String,
    ) : SScanOption()

    @RedisOption
    class COUNT(
        count: Long,
    ) : SScanOption()
}
