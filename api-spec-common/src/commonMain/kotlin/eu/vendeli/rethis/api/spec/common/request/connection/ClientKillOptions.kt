package eu.vendeli.rethis.api.spec.common.request.connection

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer
import kotlinx.datetime.Instant

@RedisOptionContainer
sealed class ClientKillOptions {
    @RedisOption.Token("ADDR")
    data class Address(val ip: String, val port: Int) : ClientKillOptions()

    @RedisOption.Token("LADDR")
    data class LAddr(val ip: String, val port: Int) : ClientKillOptions()

    @RedisOption.Token("ID")
    data class Id(val clientId: Long) : ClientKillOptions()

    @RedisOption.Token("TYPE")
    data class Type(val connectionType: String) : ClientKillOptions()

    @RedisOption.Token("USER")
    data class User(val username: String) : ClientKillOptions()

    @RedisOption.Token("SKIPME")
    data class SkipMe(val yes: Boolean) : ClientKillOptions()

    @RedisOption.Token("MAXAGE")
    data class MaxAge(val instant: Instant) : ClientKillOptions()
}
