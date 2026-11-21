package eu.vendeli.rethis.core

import io.ktor.network.tls.*

// ktor-network-tls has no TLS implementation on this target, so there is nothing to configure.
internal actual fun TLSConfig.withDefaultServerName(host: String): TLSConfig = this
