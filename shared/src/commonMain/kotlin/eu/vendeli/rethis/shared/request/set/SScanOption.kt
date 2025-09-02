package eu.vendeli.rethis.shared.request.set

import eu.vendeli.rethis.shared.annotations.RedisOption

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
