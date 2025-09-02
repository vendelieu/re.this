package eu.vendeli.rethis.configuration

import eu.vendeli.rethis.annotations.ConfigurationDSL

/**
 * Configuration for redis connection socket options.
 *
 * @property timeout the timeout in milliseconds for the redis connection, defaults to null
 * @property linger SO_LINGER option for the redis connection, defaults to null,
 * [see](https://api.ktor.io/ktor-network/io.ktor.network.sockets/-socket-options/-t-c-p-client-socket-options/linger-seconds.html).
 * @property noDelay TCP_NODELAY option for the redis connection, defaults to true
 * @property keepAlive TCP_KEEPALIVE option for the redis connection, defaults to true
 */
@ConfigurationDSL
data class SocketConfiguration(
    var timeout: Long? = null,
    var linger: Int? = null,
    var noDelay: Boolean = true,
    var keepAlive: Boolean = true,
)
