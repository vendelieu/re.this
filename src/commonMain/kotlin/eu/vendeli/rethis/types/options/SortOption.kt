package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.PairArgument
import eu.vendeli.rethis.types.core.TripleArgument

sealed class SortOption {
    data class BY(
        val pattern: String,
    ) : SortOption(),
        PairArgument<String, String> {
        override val arg = "BY" to pattern
    }

    data class LIMIT(
        val offset: Long,
        val count: Long,
    ) : SortOption(),
        TripleArgument<String, Long, Long> {
        override val arg = Triple("LIMIT", offset, count)
    }

    data class GET(
        val pattern: String,
    ) : SortOption(),
        PairArgument<String, String> {
        override val arg = "GET" to pattern
    }

    sealed class Order : SortOption()
    data object ASC : Order()
    data object DESC : Order()

    data object ALPHA : SortOption()

    data class STORE(
        val destination: String,
    ) : SortOption(),
        PairArgument<String, String> {
        override val arg = "STORE" to destination
    }
}
