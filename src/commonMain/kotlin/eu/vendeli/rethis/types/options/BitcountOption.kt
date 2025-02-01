package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.Argument
import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArgument

sealed class BitcountOption {
    class Range(
        start: Int,
        end: Int,
    ) : BitcountOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf(start.toArgument(), end.toArgument())
    }
}

enum class BitmapDataType {
    BYTE,
    BIT,
}
