package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.Argument
import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArg

sealed class BitfieldOption {
    class GET(
        encoding: String,
        offset: Long,
    ) : BitfieldOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("GET".toArg(), encoding.toArg(), offset.toArg())
    }

    class OVERFLOW(
        type: Type,
    ) : BitfieldOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("OVERFLOW".toArg(), type.name.toArg())

        enum class Type { WRAP, SAT, FAIL }
    }

    class SET(
        encoding: String,
        offset: Long,
        value: Long,
    ) : BitfieldOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("SET".toArg(), encoding.toArg(), offset.toArg(), value.toArg())
    }

    class INCRBY(
        encoding: String,
        offset: Long,
        increment: Long,
    ) : BitfieldOption(),
        VaryingArgument {
        override val data: List<Argument> =
            listOf("INCRBY".toArg(), encoding.toArg(), offset.toArg(), increment.toArg())
    }
}
