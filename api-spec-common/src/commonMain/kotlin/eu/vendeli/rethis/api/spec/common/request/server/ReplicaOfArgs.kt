package eu.vendeli.rethis.api.spec.common.request.server

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

sealed class ReplicaOfArgs {
    class HostPort(val host: String, val port: Long) : ReplicaOfArgs()

    @RedisOption.Token("NO")
    @RedisOption.Token("ONE")
    data object NoOne : ReplicaOfArgs()
}
