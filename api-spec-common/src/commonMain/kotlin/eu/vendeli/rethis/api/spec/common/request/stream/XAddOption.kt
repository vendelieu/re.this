package eu.vendeli.rethis.api.spec.common.request.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer

@RedisOptionContainer
sealed class XAddOption {
    @RedisOption
    class Trim(
        val strategy: TrimmingStrategy,
        val operator: Exactement? = null,
        val threshold: String,
        val limit: XOption.Limit? = null,
    ) : XAddOption()

    @RedisOptionContainer
    sealed class Identifier : XAddOption()

    @RedisOption
    class Id(
        val id: String,
    ) : Identifier()

    @RedisOption.Token("*")
    data object Asterisk : Identifier()
}
