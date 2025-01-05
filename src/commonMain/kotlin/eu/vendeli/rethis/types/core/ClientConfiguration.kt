package eu.vendeli.rethis.types.core

import eu.vendeli.rethis.annotations.ConfigurationDSL
import io.ktor.network.tls.*
import io.ktor.utils.io.charsets.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@ConfigurationDSL
data class ClientConfiguration(
    var db: Int? = null,
    var charset: Charset = Charsets.UTF_8,
    var tlsConfig: TLSConfig? = null,
    internal var auth: AuthConfiguration? = null,
    internal val poolConfiguration: PoolConfiguration = PoolConfiguration(),
    internal val reconnectionStrategy: ReconnectionStrategyConfiguration = ReconnectionStrategyConfiguration(),
) {
    fun auth(password: String, username: String? = null) {
        auth = AuthConfiguration(password, username)
    }

    fun pool(block: PoolConfiguration.() -> Unit) {
        poolConfiguration.block()
    }

    fun reconnectionStrategy(block: ReconnectionStrategyConfiguration.() -> Unit) {
        reconnectionStrategy.block()
    }
}

@ConfigurationDSL
data class AuthConfiguration(
    var password: String,
    var username: String? = null,
)

@ConfigurationDSL
data class ReconnectionStrategyConfiguration(
    var doHealthCheck: Boolean = true,
    var reconnectAttempts: Int = 3,
    var reconnectDelay: Long = 3000L,
)

@ConfigurationDSL
data class PoolConfiguration(
    var poolSize: Int = 50,
    var dispatcher: CoroutineDispatcher = Dispatchers.IO,
)
