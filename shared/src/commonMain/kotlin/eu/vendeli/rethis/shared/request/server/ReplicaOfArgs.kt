package eu.vendeli.rethis.shared.request.server

import eu.vendeli.rethis.shared.annotations.RedisOption

sealed class ReplicaOfArgs {
    class HostPort(val host: String, val port: Long) : ReplicaOfArgs()

    @RedisOption.Token("NO")
    @RedisOption.Token("ONE")
    data object NoOne : ReplicaOfArgs()
}
