package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArgument
import kotlinx.datetime.Instant
import kotlin.time.Duration

sealed class SetOption
sealed class SetExpire : SetOption() {
    class EX(
        seconds: Duration,
    ) : SetExpire(),
        VaryingArgument {
        override val data = listOf("EX".toArgument(), seconds.inWholeSeconds.toArgument())
    }

    class PX(
        milliseconds: Duration,
    ) : SetExpire(),
        VaryingArgument {
        override val data = listOf("PX".toArgument(), milliseconds.inWholeMilliseconds.toArgument())
    }

    class EXAT(
        instant: Instant,
    ) : SetExpire(),
        VaryingArgument {
        override val data = listOf("EXAT".toArgument(), instant.epochSeconds.toArgument())
    }

    class PXAT(
        instant: Instant,
    ) : SetExpire(),
        VaryingArgument {
        override val data = listOf("PXAT".toArgument(), instant.toEpochMilliseconds().toArgument())
    }

    data object KEEPTTL : SetExpire()
}

sealed class UpsertMode : SetOption() {
    data object NX : UpsertMode()
    data object XX : UpsertMode()
}

data object GET : SetOption()
