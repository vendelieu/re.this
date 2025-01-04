package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.Argument
import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArg

sealed class XClaimOption {
    class Idle(
        ms: Long,
    ) : XClaimOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("IDLE".toArg(), ms.toArg())
    }

    class Time(
        unixTimeMillis: Long,
    ) : XClaimOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("TIME".toArg(), unixTimeMillis.toArg())
    }

    class RetryCount(
        count: Long,
    ) : XClaimOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("RETRYCOUNT".toArg(), count.toArg())
    }

    class LastId(
        id: String,
    ) : XClaimOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("LASTID".toArg(), id.toArg())
    }
}
