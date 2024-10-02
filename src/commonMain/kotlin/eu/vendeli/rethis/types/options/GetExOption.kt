package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.PairArgument
import kotlin.time.Duration

sealed class GetExOption {
    class EX(
        seconds: Duration,
    ) : GetExOption(),
        PairArgument<String, Long> {
        override val arg = "EX" to seconds.inWholeSeconds
    }

    class PX(
        milliseconds: Duration,
    ) : GetExOption(),
        PairArgument<String, Long> {
        override val arg = "PX" to milliseconds.inWholeMilliseconds
    }

    class EXAT(
        seconds: Duration,
    ) : GetExOption(),
        PairArgument<String, Long> {
        override val arg = "EXAT" to seconds.inWholeSeconds
    }

    class PXAT(
        milliseconds: Duration,
    ) : GetExOption(),
        PairArgument<String, Long> {
        override val arg = "PXAT" to milliseconds.inWholeMilliseconds
    }

    data object Persist : GetExOption()
}
