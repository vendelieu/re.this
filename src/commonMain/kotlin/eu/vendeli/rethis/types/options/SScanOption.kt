package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.Argument
import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArg

sealed class SScanOption {
    data class MATCH(
        val pattern: String,
    ) : SScanOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("MATCH".toArg(), pattern.toArg())
    }

    data class COUNT(
        val count: Long,
    ) : SScanOption(),
        VaryingArgument {
        override val data = listOf("COUNT".toArg(), count.toArg())
    }
}
