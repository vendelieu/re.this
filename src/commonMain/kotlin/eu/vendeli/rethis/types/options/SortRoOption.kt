package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.Argument
import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArg

sealed class SortRoOption {
    class BY(
        pattern: String,
    ) : SortRoOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("BY".toArg(), pattern.toArg())
    }

    class LIMIT(
        offset: Long,
        count: Long,
    ) : SortRoOption(),
        VaryingArgument {
        override val data = listOf("LIMIT".toArg(), offset.toArg(), count.toArg())
    }

    class GET(
        pattern: String,
    ) : SortRoOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("GET".toArg(), pattern.toArg())
    }

    sealed class Order : SortRoOption()
    data object ASC : Order()
    data object DESC : Order()

    data object ALPHA : SortRoOption()
}
