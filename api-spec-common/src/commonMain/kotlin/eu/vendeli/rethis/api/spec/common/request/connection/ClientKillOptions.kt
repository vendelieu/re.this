package eu.vendeli.rethis.api.spec.common.request.connection

import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.types.TimeUnit
import kotlin.time.Instant


sealed class ClientKillOptions {
    @RedisOption.Token("ADDR")
    class Address(@RedisOption.Name("addr") val ipPort: String) : ClientKillOptions()

    @RedisOption.Token("LADDR")
    class LAddr(@RedisOption.Name("laddr") val ipPort: String) : ClientKillOptions()

    @RedisOption.Token("ID")
    class Id(val clientId: Long) : ClientKillOptions()

    @RedisOption.Token("USER")
    class User(val username: String) : ClientKillOptions()

    @RedisOption.Token("SKIPME")
    sealed class SkipMe : ClientKillOptions() {
        @RedisOption.Token("YES")
        data object Yes : SkipMe()

        @RedisOption.Token("NO")
        data object No : SkipMe()
    }

    @RedisOption.Token("MAXAGE")
    class MaxAge(
        @RedisOption.Name("maxage") val instant: @RedisMeta.OutgoingTimeUnit(TimeUnit.SECONDS) Instant,
    ) : ClientKillOptions()
}

@RedisOption.Token("TYPE")
sealed class ClientType : ClientKillOptions() {
    @RedisOption.Token("NORMAL")
    data object Normal : ClientType()

    @RedisOption.Token("MASTER")
    data object Master : ClientType()

    @RedisOption.Token("SLAVE")
    data object Slave : ClientType()

    @RedisOption.Token("REPLICA")
    data object Replica : ClientType()

    @RedisOption.Token("PUBSUB")
    data object PubSub : ClientType()
}
