package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.Argument
import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArgument

sealed class XOption {
    class Limit(
        count: Long,
    ) : XOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("LIMIT".toArgument(), count.toArgument())
    }
}

sealed class TrimmingStrategy : XOption()
data object MAXLEN : TrimmingStrategy()
data object MINID : TrimmingStrategy()

sealed class Exactement : XOption()
data object Equal : Exactement()
data object Approximate : Exactement()

sealed class XId : XOption() {
    class Id(
        id: String,
    ) : XId(),
        VaryingArgument {
        override val data: List<Argument> = listOf(id.toArgument())
    }

    data object LastEntry : XId(), VaryingArgument {
        override val data: List<Argument> = listOf("$".toArgument())
    }
}
