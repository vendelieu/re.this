package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.Argument
import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArg

sealed class ScanOption {
    data class Match(
        val pattern: String,
    ) : ScanOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("MATCH".toArg(), pattern.toArg())
    }

    data class Count(
        val count: Long,
    ) : ScanOption(),
        VaryingArgument {
        override val data = listOf("COUNT".toArg(), count.toArg())
    }

    data class Type(
        val type: String,
    ) : ScanOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("TYPE".toArg(), type.toArg())
    }
}
