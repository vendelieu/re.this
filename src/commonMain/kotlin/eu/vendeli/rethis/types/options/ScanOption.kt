package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.Argument
import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArg

sealed class ScanOption {
    class Match(
        pattern: String,
    ) : ScanOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("MATCH".toArg(), pattern.toArg())
    }

    class Count(
        count: Long,
    ) : ScanOption(),
        VaryingArgument {
        override val data = listOf("COUNT".toArg(), count.toArg())
    }

    class Type(
        val type: String,
    ) : ScanOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("TYPE".toArg(), type.toArg())
    }
}
