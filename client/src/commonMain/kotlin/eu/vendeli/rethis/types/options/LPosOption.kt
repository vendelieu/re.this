package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.common.VaryingArgument
import eu.vendeli.rethis.types.common.toArgument

sealed class LPosOption {
    sealed class CommonOption : LPosOption()

    class Rank(
        rank: Long,
    ) : CommonOption(),
        VaryingArgument {
        override val data = listOf("RANK".toArgument(), rank.toArgument())
    }

    class MaxLen(
        maxLen: Long,
    ) : CommonOption(),
        VaryingArgument {
        override val data = listOf("MAXLEN".toArgument(), maxLen.toArgument())
    }

    class Count(
        count: Long,
    ) : LPosOption(),
        VaryingArgument {
        override val data = listOf("COUNT".toArgument(), count.toArgument())
    }
}
