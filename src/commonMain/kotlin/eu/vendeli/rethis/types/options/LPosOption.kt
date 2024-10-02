package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.PairArgument

sealed class LPosOption {
    sealed class CommonOption : LPosOption()

    data class Rank(
        val rank: Long,
    ) : CommonOption(),
        PairArgument<String, Long> {
        override val arg = "RANK" to rank
    }

    data class MaxLen(
        val maxLen: Long,
    ) : CommonOption(),
        PairArgument<String, Long> {
        override val arg = "MAXLEN" to maxLen
    }

    data class Count(
        val count: Long,
    ) : LPosOption(),
        PairArgument<String, Long> {
        override val arg = "COUNT" to count
    }
}
