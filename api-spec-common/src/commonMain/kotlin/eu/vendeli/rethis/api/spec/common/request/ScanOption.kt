package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

sealed class ScanOption {
    @RedisOption.Name("MATCH")
    class Match(
        val pattern: String,
    ) : ScanOption()

    @RedisOption.Name("COUNT")
    class Count(
        val count: Long,
    ) : ScanOption()

    @RedisOption.Name("TYPE")
    class Type(
        val type: String,
    ) : ScanOption()
}
