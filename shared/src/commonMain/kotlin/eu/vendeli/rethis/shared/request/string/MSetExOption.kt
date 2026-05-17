package eu.vendeli.rethis.shared.request.string

import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.types.TimeUnit
import kotlin.time.Duration
import kotlin.time.Instant

sealed class MSetExOption {

    sealed class Condition : MSetExOption() {
        data object NX : Condition()
        data object XX : Condition()
    }

    sealed class Expiration : MSetExOption() {
        @RedisOption.Token("EX")
        class Ex(val seconds: @RedisMeta.OutgoingTimeUnit(TimeUnit.SECONDS) Duration) : Expiration()

        @RedisOption.Token("PX")
        class Px(val milliseconds: @RedisMeta.OutgoingTimeUnit(TimeUnit.MILLISECONDS) Duration) : Expiration()

        @RedisOption.Token("EXAT")
        class ExAt(val unixTimeSeconds: @RedisMeta.OutgoingTimeUnit(TimeUnit.SECONDS) Instant) : Expiration()

        @RedisOption.Token("PXAT")
        class PxAt(val unixTimeMilliseconds: @RedisMeta.OutgoingTimeUnit(TimeUnit.MILLISECONDS) Instant) : Expiration()

        data object KEEPTTL : Expiration()
    }
}
