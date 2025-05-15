package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

sealed class HScanOption {
    @RedisOption.Token("MATCH")
    class Match(
        val pattern: String,
    ) : HScanOption()

    @RedisOption.Token("COUNT")
    class Count(
        val count: Long,
    ) : HScanOption()

    @RedisOption
    data object NOVALUES : HScanOption()
}
