package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArgument
import kotlin.time.Duration

sealed class RestoreOption {
    data object REPLACE : RestoreOption()
    data object ABSTTL : RestoreOption()
    class IDLETIME(
        val seconds: Duration,
    ) : RestoreOption(),
        VaryingArgument {
        override val data = listOf("IDLETIME".toArgument(), seconds.inWholeSeconds.toArgument())
    }

    class FREQ(
        frequency: Long,
    ) : RestoreOption(),
        VaryingArgument {
        override val data = listOf("FREQ".toArgument(), frequency.toArgument())
    }
}
