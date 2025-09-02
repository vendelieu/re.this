package eu.vendeli.rethis.shared.request.sortedset

import eu.vendeli.rethis.shared.annotations.RedisOption


sealed class ZRangeOption {

    sealed class Type : ZRangeOption()

    data object BYSCORE : Type()

    data object BYLEX : Type()

    @RedisOption.Token("LIMIT")
    class Limit(
        val offset: Long,
        val count: Long,
    ) : ZRangeOption()
}
