package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.PairArgument

sealed class CopyOption {
    data class DB(
        val destination: Long,
    ) : CopyOption(),
        PairArgument<String, Long> {
        override val arg = "DB" to destination
    }
    data object REPLACE : CopyOption()
}
