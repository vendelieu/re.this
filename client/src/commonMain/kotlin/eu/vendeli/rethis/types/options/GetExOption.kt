package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.common.VaryingArgument
import eu.vendeli.rethis.types.common.toArgument
import kotlin.time.Duration

sealed class GetExOption {
    class EX(
        seconds: Duration,
    ) : GetExOption(),
        VaryingArgument {
        override val data = listOf("EX".toArgument(), seconds.inWholeSeconds.toArgument())
    }

    class PX(
        milliseconds: Duration,
    ) : GetExOption(),
        VaryingArgument {
        override val data = listOf("PX".toArgument(), milliseconds.inWholeMilliseconds.toArgument())
    }

    class EXAT(
        seconds: Duration,
    ) : GetExOption(),
        VaryingArgument {
        override val data = listOf("EXAT".toArgument(), seconds.inWholeSeconds.toArgument())
    }

    class PXAT(
        milliseconds: Duration,
    ) : GetExOption(),
        VaryingArgument {
        override val data = listOf("PXAT".toArgument(), milliseconds.inWholeMilliseconds.toArgument())
    }

    data object Persist : GetExOption()
}
