package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer

sealed class XOption {
    @RedisOption.Name("LIMIT")
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

@RedisOption.Name("=")
data object Equal : Exactement()

@RedisOption.Name("~")
data object Approximate : Exactement()

@RedisOptionContainer
sealed class XId : XOption() {
    @RedisOption.SkipName
    class Id(
        val id: String,
    ) : XId()

    @RedisOption.Name("$")
    data object LastEntry : XId()
}
