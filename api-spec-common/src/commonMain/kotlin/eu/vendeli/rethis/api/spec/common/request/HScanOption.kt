package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

sealed class HScanOption {
    @RedisOption.Name("MATCH")
    class Match(
        pattern: String,
    ) : HScanOption()

    @RedisOption.Name("COUNT")
    class Count(
        count: Long,
    ) : HScanOption()

    @RedisOption
    data object NOVALUES : HScanOption()
}
