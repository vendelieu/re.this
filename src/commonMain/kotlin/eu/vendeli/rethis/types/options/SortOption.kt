package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.Argument
import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArg

sealed class SortOption {
    data class BY(
        val pattern: String,
    ) : SortOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("BY".toArg(), pattern.toArg())
    }

    data class LIMIT(
        val offset: Long,
        val count: Long,
    ) : SortOption(),
        VaryingArgument {
        override val data = listOf("LIMIT".toArg(), offset.toArg(), count.toArg())
    }

    data class GET(
        val pattern: String,
    ) : SortOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("GET".toArg(), pattern.toArg())
    }

    sealed class Order : SortOption()
    data object ASC : Order()
    data object DESC : Order()

    data object ALPHA : SortOption()

    data class STORE(
        val destination: String,
    ) : SortOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("STORE".toArg(), destination.toArg())
    }
}
