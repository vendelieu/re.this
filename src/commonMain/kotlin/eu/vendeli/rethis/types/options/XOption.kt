package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.Argument
import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArg

sealed class XOption {
    class Limit(count: Long) : XOption(), VaryingArgument {
        override val data: List<Argument> = listOf("LIMIT".toArg(), count.toArg())
    }
}

sealed class TrimmingStrategy : XOption()
data object MAXLEN : TrimmingStrategy()
data object MINID : TrimmingStrategy()

sealed class Exactement : XOption()
data object Equal : Exactement()
data object Approximate : Exactement()

sealed class XId : XOption() {
    class Id(id: String) : XId(), VaryingArgument {
        override val data: List<Argument> = listOf(id.toArg())
    }

    data object LastEntry : XId(), VaryingArgument {
        override val data: List<Argument> = listOf("$".toArg())
    }
}
