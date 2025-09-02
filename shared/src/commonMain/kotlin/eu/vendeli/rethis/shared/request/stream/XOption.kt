package eu.vendeli.rethis.shared.request.stream

import eu.vendeli.rethis.shared.annotations.RedisOption

sealed class XOption

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
