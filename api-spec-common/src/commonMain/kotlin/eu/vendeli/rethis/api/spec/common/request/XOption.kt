package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer

sealed class XOption {
    @RedisOption.Token("LIMIT")
    class Limit(
        val count: Long,
    ) : XOption()
}

@RedisOptionContainer
sealed class TrimmingStrategy : XOption()

@RedisOption
data object MAXLEN : TrimmingStrategy()

@RedisOption
data object MINID : TrimmingStrategy()

@RedisOptionContainer
sealed class Exactement : XOption()

@RedisOption.Token("=")
data object Equal : Exactement()

@RedisOption.Token("~")
data object Approximate : Exactement()

@RedisOptionContainer
sealed class XId : XOption() {
    @RedisOption
    class Id(
        val id: String,
    ) : XId()

    @RedisOption.Token("$")
    data object LastEntry : XId()
}
