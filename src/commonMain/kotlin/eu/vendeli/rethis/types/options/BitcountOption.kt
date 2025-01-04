package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.Argument
import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArg

sealed class BitcountOption {
    class Range(start: Int, end: Int) : BitcountOption(), VaryingArgument {
        override val data: List<Argument> = listOf(start.toArg(), end.toArg())
    }
}

enum class BitmapDataType {
    BYTE,
    BIT,
}
