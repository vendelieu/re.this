package eu.vendeli.rethis.shared.request.generic

import eu.vendeli.rethis.shared.annotations.RedisOption

sealed class ScanOption {
    @RedisOption.Token("MATCH")
    class Match(
        val pattern: String,
    ) : ScanOption()

    @RedisOption.Token("COUNT")
    class Count(
        val count: Long,
    ) : ScanOption()

    @RedisOption.Token("TYPE")
    class Type(
        val type: String,
    ) : ScanOption()
}
