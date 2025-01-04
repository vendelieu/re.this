package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArg

sealed class CopyOption {
    class DB(
        destination: Long,
    ) : CopyOption(),
        VaryingArgument {
        override val data = listOf("DB".toArg(), destination.toArg())
    }

    data object REPLACE : CopyOption()
}
