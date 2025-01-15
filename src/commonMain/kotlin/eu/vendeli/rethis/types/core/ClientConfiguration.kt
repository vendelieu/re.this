package eu.vendeli.rethis.types.core

import eu.vendeli.rethis.annotations.ConfigurationDSL
import io.ktor.network.tls.*
import io.ktor.utils.io.charsets.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * A configuration class for the client.
 *
 * @property db The database index to switch to after connecting.
 * @property charset The character set to use for communication.
 * @property tlsConfig The TLS configuration to use when connecting.
 * @property auth The authentication options.
 * @property poolConfiguration The pool configuration.
 * @property reconnectionStrategy The reconnection strategy to use.
 */
@ConfigurationDSL
data class ClientConfiguration(
    var db: Int? = null,
    var charset: Charset = Charsets.UTF_8,
    var tlsConfig: TLSConfig? = null,
    internal var auth: AuthConfiguration? = null,
    internal val poolConfiguration: PoolConfiguration = PoolConfiguration(),
    internal val reconnectionStrategy: ReconnectionStrategyConfiguration = ReconnectionStrategyConfiguration(),
    internal val socketConfiguration: SocketConfiguration = SocketConfiguration()
) {
    /**
     * Configures authentication for the client.
     *
     * @param password The password to use for authentication.
     * @param username The username to use for authentication, defaults to null.
     */
    fun auth(password: String, username: String? = null) {
        auth = AuthConfiguration(password, username)
    }

    /**
     * Configures the connection pool settings.
     *
     * @param block A lambda to configure the pool settings.
     */
    fun pool(block: PoolConfiguration.() -> Unit) {
        poolConfiguration.block()
    }

    /**
     * Configures the reconnection strategy.
     *
     * @param block A lambda to configure the reconnection strategy.
     */
    fun reconnectionStrategy(block: ReconnectionStrategyConfiguration.() -> Unit) {
        reconnectionStrategy.block()
    }

    /**
     * Configures the socket options.
     *
     * @param block A lambda to configure the socket settings.
     */
    fun socket(block: SocketConfiguration.() -> Unit) {
        socketConfiguration.block()
    }
}

/**
 * Configuration for redis connection authentication.
 *
 * @property password the password to use for authentication
 * @property username the username to use for authentication, defaults to null
 */
@ConfigurationDSL
data class AuthConfiguration(
    var password: String,
    var username: String? = null,
)

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

/**
 * Configuration for redis connection reconnection strategy.
 *
 * @property doHealthCheck if true, performs health checks on the connection before adding it to the pool, defaults to true
 * @property reconnectAttempts the number of times to attempt reconnecting to the redis server on failure, defaults to 3
 * @property reconnectDelay the delay in milliseconds between reconnecting, defaults to 3000L
 */
@ConfigurationDSL
data class ReconnectionStrategyConfiguration(
    var doHealthCheck: Boolean = true,
    var reconnectAttempts: Int = 3,
    var reconnectDelay: Long = 3000L,
)

/**
 * Configuration for redis connection pool.
 *
 * @property poolSize the size of the connection pool, defaults to 50
 * @property dispatcher the dispatcher to use for connection pool coroutines, defaults to [Dispatchers.IO]
 */
@ConfigurationDSL
data class PoolConfiguration(
    var poolSize: Int = 50,
    var dispatcher: CoroutineDispatcher = Dispatchers.IO,
)
