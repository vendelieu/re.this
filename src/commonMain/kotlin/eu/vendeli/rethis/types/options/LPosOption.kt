package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArg

sealed class LPosOption {
    sealed class CommonOption : LPosOption()

    data class Rank(
        val rank: Long,
    ) : CommonOption(),
        VaryingArgument {
        override val data = listOf("RANK".toArg(), rank.toArg())
    }

    data class MaxLen(
        val maxLen: Long,
    ) : CommonOption(),
        VaryingArgument {
        override val data = listOf("MAXLEN".toArg(), maxLen.toArg())
    }

    data class Count(
        val count: Long,
    ) : LPosOption(),
        VaryingArgument {
        override val data = listOf("COUNT".toArg(), count.toArg())
    }
}
