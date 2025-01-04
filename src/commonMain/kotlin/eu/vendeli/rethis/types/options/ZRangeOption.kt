package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArg

sealed class ZRangeOption {
    sealed class Type : ZRangeOption()
    data object BYSCORE : Type()
    data object BYLEX : Type()

    class LIMIT(
        offset: Long,
        count: Long,
    ) : ZRangeOption(),
        VaryingArgument {
        override val data = listOf("LIMIT".toArg(), offset.toArg(), count.toArg())
    }
}
