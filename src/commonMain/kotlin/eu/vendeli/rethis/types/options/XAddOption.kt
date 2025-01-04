package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.Argument
import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArg
import eu.vendeli.rethis.utils.writeArg

sealed class XAddOption {
    data object NOMKSTREAM : XAddOption()

    class Trim(
        strategy: TrimmingStrategy,
        threshold: Long,
        exactement: Exactement? = null,
        limit: XOption.Limit? = null,
    ) : XAddOption(), VaryingArgument {
        override val data: List<Argument> = mutableListOf(
            strategy.toArg(),
        ).writeArg(
            exactement,
            threshold,
            limit
        )
    }

    sealed class Identifier : XAddOption()
    class Id(
        id: String,
    ) : Identifier(), VaryingArgument {
        override val data: List<Argument> = listOf(id.toArg())
    }

    data object Asterisk : Identifier(), VaryingArgument {
        override val data: List<Argument> = listOf("*".toArg())
    }
}
