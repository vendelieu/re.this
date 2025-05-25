package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

sealed class SScanOption {
    @RedisOption.Token("MATCH")
    class Match(
        val pattern: String,
    ) : SScanOption()

    @RedisOption.Token("COUNT")
    class Count(
        val count: Long,
    ) : SScanOption()
}
