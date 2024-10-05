package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.Argument
import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArg

sealed class HScanOption {
    data class Match(
        val pattern: String,
    ) : HScanOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("MATCH".toArg(), pattern.toArg())
    }

    data class Count(
        val count: Long,
    ) : HScanOption(),
        VaryingArgument {
        override val data = listOf("COUNT".toArg(), count.toArg())
    }

    data object NOVALUES : HScanOption()
}
