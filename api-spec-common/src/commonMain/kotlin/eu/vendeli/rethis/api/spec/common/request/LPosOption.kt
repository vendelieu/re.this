package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer

sealed class LPosOption {
    @RedisOptionContainer
    sealed class CommonOption : LPosOption()

    @RedisOption.Name("RANK")
    class Rank(
        val rank: Long,
    ) : CommonOption()

    @RedisOption.Name("MAXLEN")
    class MaxLen(
        val maxLen: Long,
    ) : CommonOption()

    @RedisOption.Name("COUNT")
    class Count(
        val count: Long,
    ) : LPosOption()
}
