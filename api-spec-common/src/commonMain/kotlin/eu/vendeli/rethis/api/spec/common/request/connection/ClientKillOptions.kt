package eu.vendeli.rethis.api.spec.common.request.connection

import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.types.TimeUnit
import kotlin.time.Instant


sealed class ClientKillOptions {
    @RedisOption.Token("ADDR")
    class Address(val ip: String, val port: Int) : ClientKillOptions()

    @RedisOption.Token("LADDR")
    class LAddr(val ip: String, val port: Int) : ClientKillOptions()

    @RedisOption.Token("ID")
    class Id(val clientId: Long) : ClientKillOptions()

    @RedisOption.Token("TYPE")
    class Type(val connectionType: String) : ClientKillOptions()

    @RedisOption.Token("USER")
    class User(val username: String) : ClientKillOptions()

    @RedisOption.Token("SKIPME")
    class SkipMe(val yes: Boolean) : ClientKillOptions()

    @RedisOption.Token("MAXAGE")
    class MaxAge(val instant: @RedisMeta.OutgoingTimeUnit(TimeUnit.SECONDS) Instant) : ClientKillOptions()
}
