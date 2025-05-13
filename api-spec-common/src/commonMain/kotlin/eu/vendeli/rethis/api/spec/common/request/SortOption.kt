package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer

sealed class SortOption {
    @RedisOption
    class BY(
        val pattern: String,
    ) : SortOption()

    @RedisOption
    class LIMIT(
        val offset: Long,
        val count: Long,
    ) : SortOption()

    @RedisOption
    class GET(
        val pattern: String,
    ) : SortOption()

    @RedisOptionContainer
    sealed class Order : SortOption()

    @RedisOption
    data object ASC : Order()

    @RedisOption
    data object DESC : Order()

    @RedisOption
    data object ALPHA : SortOption()

    @RedisOption
    class STORE(
        val destination: String,
    ) : SortOption()
}
