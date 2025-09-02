package eu.vendeli.rethis.shared.request.list

import eu.vendeli.rethis.shared.annotations.RedisOption

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
