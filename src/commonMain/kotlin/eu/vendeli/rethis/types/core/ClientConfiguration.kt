package eu.vendeli.rethis.types.core

import io.ktor.network.tls.*
import io.ktor.utils.io.charsets.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

data class ClientConfiguration(
    var auth: AuthConfiguration? = null,
    var db: Int? = null,
    var charset: Charset = Charsets.UTF_8,
    var maxConnections: Int = 50,
    var tlsConfig: TLSConfig? = null,
    var dispatcher: CoroutineDispatcher = Dispatchers.IO,
)

data class AuthConfiguration(
    var password: String,
    var username: String? = null,
)
