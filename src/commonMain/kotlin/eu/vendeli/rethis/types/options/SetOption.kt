package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.PairArgument
import kotlinx.datetime.Instant
import kotlin.time.Duration

sealed class SetOption
sealed class SetExpire : SetOption() {
    class EX(
        seconds: Duration,
    ) : SetExpire(),
        PairArgument<String, Long> {
        override val arg = "EX" to seconds.inWholeSeconds
    }

    class PX(
        milliseconds: Duration,
    ) : SetExpire(),
        PairArgument<String, Long> {
        override val arg = "PX" to milliseconds.inWholeMilliseconds
    }

    class EXAT(
        instant: Instant,
    ) : SetExpire(),
        PairArgument<String, Long> {
        override val arg = "EXAT" to instant.epochSeconds
    }

    class PXAT(
        instant: Instant,
    ) : SetExpire(),
        PairArgument<String, Long> {
        override val arg = "PXAT" to instant.toEpochMilliseconds()
    }

    data object KEEPTTL : SetExpire()
}

sealed class UpsertMode : SetOption() {
    data object NX : UpsertMode()
    data object XX : UpsertMode()
}

data object GET : SetOption()
