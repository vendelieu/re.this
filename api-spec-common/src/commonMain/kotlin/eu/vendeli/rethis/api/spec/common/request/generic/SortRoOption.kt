package eu.vendeli.rethis.api.spec.common.request.generic

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

sealed class SortRoOption {
    @RedisOption.Token("BY")
    class By(
        val byPattern: String,
    ) : SortRoOption()

    @RedisOption.Token("LIMIT")
    class Limit(
        val offset: Long,
        val count: Long,
    ) : SortRoOption()

    @RedisOption.Token("GET")
    class Get(
        vararg val getPattern: String,
    ) : SortRoOption()


    sealed class Order : SortRoOption()

    data object ASC : Order()

    data object DESC : Order()

    data object ALPHA : SortRoOption()
}
