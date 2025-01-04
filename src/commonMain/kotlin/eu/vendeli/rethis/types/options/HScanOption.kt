package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.Argument
import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArg

sealed class HScanOption {
    class Match(
        pattern: String,
    ) : HScanOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("MATCH".toArg(), pattern.toArg())
    }

    class Count(
        count: Long,
    ) : HScanOption(),
        VaryingArgument {
        override val data = listOf("COUNT".toArg(), count.toArg())
    }

    data object NOVALUES : HScanOption()
}
