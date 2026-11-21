package eu.vendeli.rethis.core

import io.ktor.network.tls.*

internal actual fun TLSConfig.withDefaultServerName(host: String): TLSConfig =
    if (serverName != null) this else TLSConfig(random, certificates, trustManager, cipherSuites, host)
