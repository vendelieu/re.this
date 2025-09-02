package eu.vendeli.rethis.shared.request.stream

import eu.vendeli.rethis.shared.annotations.RedisOption


sealed class XAddOption {
    class Trim(
        val strategy: TrimmingStrategy,
        val operator: Exactement? = null,
        val threshold: String,
        @RedisOption.Token("LIMIT") val count: Long? = null,
    ) : XAddOption()

    sealed class Identifier : XAddOption()

    class Id(
        val id: String,
    ) : Identifier()

    @RedisOption.Token("*")
    data object Asterisk : Identifier()
}
