package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArg
import kotlin.time.Duration

sealed class GetExOption {
    class EX(
        seconds: Duration,
    ) : GetExOption(),
        VaryingArgument {
        override val data = listOf("EX".toArg(), seconds.inWholeSeconds.toArg())
    }

    class PX(
        milliseconds: Duration,
    ) : GetExOption(),
        VaryingArgument {
        override val data = listOf("PX".toArg(), milliseconds.inWholeMilliseconds.toArg())
    }

    class EXAT(
        seconds: Duration,
    ) : GetExOption(),
        VaryingArgument {
        override val data = listOf("EXAT".toArg(), seconds.inWholeSeconds.toArg())
    }

    class PXAT(
        milliseconds: Duration,
    ) : GetExOption(),
        VaryingArgument {
        override val data = listOf("PXAT".toArg(), milliseconds.inWholeMilliseconds.toArg())
    }

    data object Persist : GetExOption()
}
