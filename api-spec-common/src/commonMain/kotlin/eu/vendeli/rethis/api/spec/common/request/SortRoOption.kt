package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer

sealed class SortRoOption {
    @RedisOption
    class BY(
        val pattern: String,
    ) : SortRoOption()

    @RedisOption
    class LIMIT(
        val offset: Long,
        val count: Long,
    ) : SortRoOption()

    @RedisOption
    class GET(
        val pattern: String,
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
