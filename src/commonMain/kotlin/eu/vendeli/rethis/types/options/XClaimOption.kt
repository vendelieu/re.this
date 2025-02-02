package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.Argument
import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArgument

sealed class XClaimOption {
    class Idle(
        ms: Long,
    ) : XClaimOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("IDLE".toArgument(), ms.toArgument())
    }

    class Time(
        unixTimeMillis: Long,
    ) : XClaimOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("TIME".toArgument(), unixTimeMillis.toArgument())
    }

    class RetryCount(
        count: Long,
    ) : XClaimOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("RETRYCOUNT".toArgument(), count.toArgument())
    }

    class LastId(
        id: String,
    ) : XClaimOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("LASTID".toArgument(), id.toArgument())
    }
}
