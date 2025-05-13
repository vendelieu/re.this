package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.common.Argument
import eu.vendeli.rethis.types.common.VaryingArgument
import eu.vendeli.rethis.types.common.toArgument

sealed class SScanOption {
    class MATCH(
        pattern: String,
    ) : SScanOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("MATCH".toArgument(), pattern.toArgument())
    }

    class COUNT(
        count: Long,
    ) : SScanOption(),
        VaryingArgument {
        override val data = listOf("COUNT".toArgument(), count.toArgument())
    }
}
