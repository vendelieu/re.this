package eu.vendeli.rethis.api.spec.common.request.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption


sealed class XAddOption {
    class Trim(
        val strategy: TrimmingStrategy,
        val operator: Exactement? = null,
        val threshold: String,
        val limit: XOption.Limit? = null,
    ) : XAddOption()


    sealed class Identifier : XAddOption()

    class Id(
        val id: String,
    ) : Identifier()

    @RedisOption.Token("*")
    data object Asterisk : Identifier()
}
