package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArg
import kotlinx.datetime.Instant
import kotlin.time.Duration

sealed class SetOption
sealed class SetExpire : SetOption() {
    class EX(
        seconds: Duration,
    ) : SetExpire(),
        VaryingArgument {
        override val data = listOf("EX".toArg(), seconds.inWholeSeconds.toArg())
    }

    class PX(
        milliseconds: Duration,
    ) : SetExpire(),
        VaryingArgument {
        override val data = listOf("PX".toArg(), milliseconds.inWholeMilliseconds.toArg())
    }

    class EXAT(
        instant: Instant,
    ) : SetExpire(),
        VaryingArgument {
        override val data = listOf("EXAT".toArg(), instant.epochSeconds.toArg())
    }

    class PXAT(
        instant: Instant,
    ) : SetExpire(),
        VaryingArgument {
        override val data = listOf("PXAT".toArg(), instant.toEpochMilliseconds().toArg())
    }

    data object KEEPTTL : SetExpire()
}

sealed class UpsertMode : SetOption() {
    data object NX : UpsertMode()
    data object XX : UpsertMode()
}

data object GET : SetOption()
