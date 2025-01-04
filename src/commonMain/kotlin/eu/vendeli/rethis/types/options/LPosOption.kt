package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArg

sealed class LPosOption {
    sealed class CommonOption : LPosOption()

    class Rank(
        rank: Long,
    ) : CommonOption(),
        VaryingArgument {
        override val data = listOf("RANK".toArg(), rank.toArg())
    }

    class MaxLen(
        maxLen: Long,
    ) : CommonOption(),
        VaryingArgument {
        override val data = listOf("MAXLEN".toArg(), maxLen.toArg())
    }

    class Count(
        count: Long,
    ) : LPosOption(),
        VaryingArgument {
        override val data = listOf("COUNT".toArg(), count.toArg())
    }
}
