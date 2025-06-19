package eu.vendeli.rethis.api.spec.common.request.list

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

sealed class LPosOption {
    @RedisOption.Token("RANK")
    class Rank(
        val rank: Long,
    ) : LPosOption()

    @RedisOption.Token("MAXLEN")
    class MaxLen(
        val len: Long,
    ) : LPosOption()
}
