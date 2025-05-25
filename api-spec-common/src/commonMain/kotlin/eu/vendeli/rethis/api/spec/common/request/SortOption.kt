package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer

sealed class SortOption {
    @RedisOption.Token("BY")
    class By(
        val pattern: String,
    ) : SortOption()

    @RedisOption.Token("LIMIT")
    class Limit(
        val offset: Long,
        val count: Long,
    ) : SortOption()

    @RedisOption.Token("GET")
    class Get(
        vararg val pattern: String,
    ) : SortOption()

    @RedisOptionContainer
    sealed class Order : SortOption()

    @RedisOption
    data object ASC : Order()

    @RedisOption
    data object DESC : Order()

    @RedisOption
    data object ALPHA : SortOption()

    @RedisOption.Token("STORE")
    class Store(
        @RedisKey val destination: String,
    ) : SortOption()
}
