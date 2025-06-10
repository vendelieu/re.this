package eu.vendeli.rethis.api.spec.common.request.generic

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer

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

    @RedisOptionContainer
    sealed class Order : SortRoOption()

    @RedisOption
    data object ASC : Order()

    @RedisOption
    data object DESC : Order()

    @RedisOption
    data object ALPHA : SortRoOption()
}
