package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.common.VaryingArgument
import eu.vendeli.rethis.types.common.toArgument

sealed class ZRangeOption {
    sealed class Type : ZRangeOption()
    data object BYSCORE : Type()
    data object BYLEX : Type()

    class LIMIT(
        offset: Long,
        count: Long,
    ) : ZRangeOption(),
        VaryingArgument {
        override val data = listOf("LIMIT".toArgument(), offset.toArgument(), count.toArgument())
    }
}
