package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArgument

sealed class CopyOption {
    class DB(
        destination: Long,
    ) : CopyOption(),
        VaryingArgument {
        override val data = listOf("DB".toArgument(), destination.toArgument())
    }

    data object REPLACE : CopyOption()
}
