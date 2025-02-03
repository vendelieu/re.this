package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.common.Argument
import eu.vendeli.rethis.types.common.VaryingArgument
import eu.vendeli.rethis.types.common.toArgument
import eu.vendeli.rethis.utils.writeArgument

sealed class XAddOption {
    data object NOMKSTREAM : XAddOption()

    class Trim(
        strategy: TrimmingStrategy,
        threshold: Long,
        exactement: Exactement? = null,
        limit: XOption.Limit? = null,
    ) : XAddOption(),
        VaryingArgument {
        override val data: List<Argument> = mutableListOf(
            strategy.toArgument(),
        ).writeArgument(
            exactement,
            threshold,
            limit,
        )
    }

    sealed class Identifier : XAddOption()
    class Id(
        id: String,
    ) : Identifier(),
        VaryingArgument {
        override val data: List<Argument> = listOf(id.toArgument())
    }

    data object Asterisk : Identifier(), VaryingArgument {
        override val data: List<Argument> = listOf("*".toArgument())
    }
}
