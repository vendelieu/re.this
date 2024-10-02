package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.PairArgument
import eu.vendeli.rethis.types.core.TripleArgument

sealed class SortRoOption {
    data class BY(
        val pattern: String,
    ) : SortRoOption(),
        PairArgument<String, String> {
        override val arg = "BY" to pattern
    }

    data class LIMIT(
        val offset: Long,
        val count: Long,
    ) : SortRoOption(),
        TripleArgument<String, Long, Long> {
        override val arg = Triple("LIMIT", offset, count)
    }

    data class GET(
        val pattern: String,
    ) : SortRoOption(),
        PairArgument<String, String> {
        override val arg = "GET" to pattern
    }

    sealed class Order : SortRoOption()
    data object ASC : Order()
    data object DESC : Order()

    data object ALPHA : SortRoOption()
}
