package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.common.Argument
import eu.vendeli.rethis.types.common.VaryingArgument
import eu.vendeli.rethis.types.common.toArgument

sealed class SortOption {
    class BY(
        pattern: String,
    ) : SortOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("BY".toArgument(), pattern.toArgument())
    }

    class LIMIT(
        offset: Long,
        count: Long,
    ) : SortOption(),
        VaryingArgument {
        override val data = listOf("LIMIT".toArgument(), offset.toArgument(), count.toArgument())
    }

    class GET(
        pattern: String,
    ) : SortOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("GET".toArgument(), pattern.toArgument())
    }

    sealed class Order : SortOption()
    data object ASC : Order()
    data object DESC : Order()

    data object ALPHA : SortOption()

    class STORE(
        destination: String,
    ) : SortOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("STORE".toArgument(), destination.toArgument())
    }
}
