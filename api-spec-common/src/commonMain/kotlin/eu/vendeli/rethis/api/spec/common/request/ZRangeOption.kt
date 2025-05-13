package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer

sealed class ZRangeOption {
    @RedisOptionContainer
    sealed class Type : ZRangeOption()

    @RedisOption
    data object BYSCORE : Type()

    @RedisOption
    data object BYLEX : Type()

    @RedisOption
    class LIMIT(
        val offset: Long,
        val count: Long,
    ) : ZRangeOption()
}
