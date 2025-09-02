package eu.vendeli.rethis.shared.request.generic

import eu.vendeli.rethis.shared.annotations.RedisOption

sealed class SortOption {
    @RedisOption.Token("BY")
    class By(
        val byPattern: String,
    ) : SortOption()

    @RedisOption.Token("LIMIT")
    class Limit(
        val offset: Long,
        val count: Long,
    ) : SortOption()

    @RedisOption.Token("GET")
    class Get(
        vararg val getPattern: String,
    ) : SortOption()


    sealed class Order : SortOption()

    data object ASC : Order()

    data object DESC : Order()

    data object ALPHA : SortOption()
}
