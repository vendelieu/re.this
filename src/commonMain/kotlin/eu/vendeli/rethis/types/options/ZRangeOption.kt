package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.TripleArgument

sealed class ZRangeOption {
    sealed class Type : ZRangeOption()
    data object BYSCORE : Type()
    data object BYLEX : Type()

    data class LIMIT(
        val offset: Long,
        val count: Long,
    ) : ZRangeOption(),
        TripleArgument<String, Long, Long> {
        override val arg = Triple("LIMIT", offset, count)
    }
}
