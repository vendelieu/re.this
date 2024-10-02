package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.PairArgument
import kotlin.time.Duration

sealed class RestoreOption {
    data object REPLACE : RestoreOption()
    data object ABSTTL : RestoreOption()
    data class IDLETIME(
        val seconds: Duration,
    ) : RestoreOption(),
        PairArgument<String, Long> {
        override val arg = "IDLETIME" to seconds.inWholeSeconds
    }

    data class FREQ(
        val frequency: Long,
    ) : RestoreOption(),
        PairArgument<String, Long> {
        override val arg = "FREQ" to frequency
    }
}
