package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import kotlinx.datetime.Instant

sealed class ClientKillOptions {
    @RedisOption.Name("ADDR")
    data class Address(val ip: String, val port: Int) : ClientKillOptions()

    @RedisOption.Name("LADDR")
    data class LAddr(val ip: String, val port: Int) : ClientKillOptions()

    @RedisOption.Name("ID")
    data class Id(val clientId: Long) : ClientKillOptions()

    @RedisOption.Name("TYPE")
    data class Type(val connectionType: String) : ClientKillOptions()

    @RedisOption.Name("USER")
    data class User(val username: String) : ClientKillOptions()

    @RedisOption.Name("SKIPME")
    data class SkipMe(val yes: Boolean) : ClientKillOptions()

    @RedisOption.Name("MAXAGE")
    data class MaxAge(val instant: Instant) : ClientKillOptions()
}
