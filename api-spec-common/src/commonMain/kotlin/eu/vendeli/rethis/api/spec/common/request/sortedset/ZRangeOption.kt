package eu.vendeli.rethis.api.spec.common.request.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption


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
