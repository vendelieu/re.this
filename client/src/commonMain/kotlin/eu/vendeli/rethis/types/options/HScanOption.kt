package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.common.Argument
import eu.vendeli.rethis.types.common.VaryingArgument
import eu.vendeli.rethis.types.common.toArgument

sealed class HScanOption {
    class Match(
        pattern: String,
    ) : HScanOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("MATCH".toArgument(), pattern.toArgument())
    }

    class Count(
        count: Long,
    ) : HScanOption(),
        VaryingArgument {
        override val data = listOf("COUNT".toArgument(), count.toArgument())
    }

    data object NOVALUES : HScanOption()
}
