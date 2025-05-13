package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer

sealed class XAddOption {
    @RedisOption
    data object NOMKSTREAM : XAddOption()

    @RedisOption.SkipName
    class Trim(
        val strategy: TrimmingStrategy,
        val exactement: Exactement? = null,
        val threshold: Long,
        val limit: XOption.Limit? = null,
    ) : XAddOption()

    @RedisOptionContainer
    sealed class Identifier : XAddOption()

    @RedisOption.SkipName
    class Id(
        val id: String,
    ) : Identifier()

    @RedisOption.Name("*")
    data object Asterisk : Identifier()
}
