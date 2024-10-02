package eu.vendeli.rethis.types.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

data class ClientConfiguration(
    var auth: AuthConfiguration? = null,
    var db: Int? = null,
    var maxConnections: Int = 50,
    var dispatcher: CoroutineDispatcher = Dispatchers.IO,
)

data class AuthConfiguration(
    var password: String,
    var username: String? = null,
)
