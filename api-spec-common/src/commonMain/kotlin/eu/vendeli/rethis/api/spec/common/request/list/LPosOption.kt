package eu.vendeli.rethis.api.spec.common.request.list

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer

sealed class LPosOption {
    @RedisOptionContainer
    sealed class CommonOption : LPosOption()

    @RedisOption.Token("RANK")
    class Rank(
        val rank: Long,
    ) : CommonOption()

    @RedisOption.Token("MAXLEN")
    class MaxLen(
        val len: Long,
    ) : CommonOption()

    @RedisOption.Token("COUNT")
    class Count(
        val numMatches: Long,
    ) : LPosOption()
}
