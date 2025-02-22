package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.common.VaryingArgument
import eu.vendeli.rethis.types.common.toArgument

sealed class CopyOption {
    class DB(
        destination: Long,
    ) : CopyOption(),
        VaryingArgument {
        override val data = listOf("DB".toArgument(), destination.toArgument())
    }

    data object REPLACE : CopyOption()
}
