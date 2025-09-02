package eu.vendeli.rethis.shared.request.hash

import eu.vendeli.rethis.shared.annotations.RedisOption

sealed class HScanOption {
    @RedisOption.Token("MATCH")
    class Match(
        val pattern: String,
    ) : HScanOption()

    @RedisOption.Token("COUNT")
    class Count(
        val count: Long,
    ) : HScanOption()

    data object NOVALUES : HScanOption()
}
