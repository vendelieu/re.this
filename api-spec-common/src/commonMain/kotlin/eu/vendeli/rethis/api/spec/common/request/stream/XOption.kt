package eu.vendeli.rethis.api.spec.common.request.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

sealed class XOption {
    @RedisOption.Token("LIMIT")
    class Limit(
        val count: Long,
    ) : XOption()
}


sealed class TrimmingStrategy : XOption()

data object MAXLEN : TrimmingStrategy()

data object MINID : TrimmingStrategy()


sealed class Exactement : XOption()

@RedisOption.Token("=")
data object Equal : Exactement()

@RedisOption.Token("~")
data object Approximate : Exactement()


sealed class XId : XOption() {
    class Id(
        val id: String,
    ) : XId()

    @RedisOption.Token("$")
    data object LastEntry : XId()
}
