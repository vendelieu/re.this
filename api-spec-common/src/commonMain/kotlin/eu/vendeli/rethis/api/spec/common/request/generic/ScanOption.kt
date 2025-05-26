package eu.vendeli.rethis.api.spec.common.request.generic

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

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
