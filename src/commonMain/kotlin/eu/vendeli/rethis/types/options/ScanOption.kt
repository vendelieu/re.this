package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.common.Argument
import eu.vendeli.rethis.types.common.VaryingArgument
import eu.vendeli.rethis.types.common.toArgument

sealed class ScanOption {
    class Match(
        pattern: String,
    ) : ScanOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("MATCH".toArgument(), pattern.toArgument())
    }

    class Count(
        count: Long,
    ) : ScanOption(),
        VaryingArgument {
        override val data = listOf("COUNT".toArgument(), count.toArgument())
    }

    class Type(
        val type: String,
    ) : ScanOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("TYPE".toArgument(), type.toArgument())
    }
}
