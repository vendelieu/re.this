package eu.vendeli.rethis.configuration

import eu.vendeli.rethis.annotations.ConfigurationDSL
import eu.vendeli.rethis.types.common.ConfigType
import io.ktor.network.tls.*
import io.ktor.utils.io.charsets.*
import kotlinx.serialization.json.Json

/**
 * A configuration class for the client.
 *
 * @property db The database index to switch to after connecting.
 * @property charset The character set to use for communication.
 * @property tlsConfig The TLS configuration to use when connecting.
 */
@ConfigurationDSL
sealed class ReThisConfiguration(
    var db: Int? = null,
    var charset: Charset = Charsets.UTF_8,
    var tlsConfig: TLSConfig? = null,
    internal var auth: AuthConfiguration? = null,
    internal val connectionConfiguration: ConnectionConfiguration = ConnectionConfiguration(),
    internal val socketConfiguration: SocketConfiguration = SocketConfiguration(),
) {
    internal abstract val type: ConfigType
    /**
     * Configures authentication for the client.
     *
     * @param password The password to use for authentication.
     * @param username The username to use for authentication, defaults to null.
     */
    fun auth(password: CharArray, username: String? = null) {
        auth = AuthConfiguration(password, username)
    }

    /**
     * Configures the connection settings.
     *
     * @param block A lambda to configure the connection settings.
     */
    fun connection(block: ConnectionConfiguration.() -> Unit) {
        connectionConfiguration.block()
    }

    /**
     * Configures the socket options.
     *
     * @param block A lambda to configure the socket settings.
     */
    fun socket(block: SocketConfiguration.() -> Unit) {
        socketConfiguration.block()
    }

    @Suppress("ktlint:standard:backing-property-naming")
    private var _jsonModule: Json? = null
    internal val jsonModule by lazy { _jsonModule ?: Json.Default }

    /**
     * Configures the JSON serializer module.
     *
     * @param module The JSON module to use for serializing and deserializing objects.
     */
    fun jsonModule(module: Json) {
        _jsonModule = module
    }
}

