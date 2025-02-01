package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.Argument
import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArgument

sealed class BitfieldOption {
    class GET(
        encoding: String,
        offset: Long,
    ) : BitfieldOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("GET".toArgument(), encoding.toArgument(), offset.toArgument())
    }

    class OVERFLOW(
        type: Type,
    ) : BitfieldOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("OVERFLOW".toArgument(), type.name.toArgument())

        enum class Type { WRAP, SAT, FAIL }
    }

    class SET(
        encoding: String,
        offset: Long,
        value: Long,
    ) : BitfieldOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("SET".toArgument(), encoding.toArgument(), offset.toArgument(), value.toArgument())
    }

    class INCRBY(
        encoding: String,
        offset: Long,
        increment: Long,
    ) : BitfieldOption(),
        VaryingArgument {
        override val data: List<Argument> =
            listOf("INCRBY".toArgument(), encoding.toArgument(), offset.toArgument(), increment.toArgument())
    }
}
