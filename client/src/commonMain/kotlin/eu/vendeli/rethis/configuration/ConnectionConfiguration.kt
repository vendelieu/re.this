package eu.vendeli.rethis.configuration

import eu.vendeli.rethis.annotations.ConfigurationDSL
import eu.vendeli.rethis.utils.IO_OR_UNCONFINED
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Configuration for redis connection.
 *
 * @property reconnectAttempts the number of times to attempt reconnecting to the redis server on failure, defaults to 3
 * @property reconnectDelay the delay in milliseconds between reconnecting, defaults to 3000L
 * @property poolSize the size of the connection pool, defaults to 50
 * @property dispatcher the dispatcher to use for connection pool coroutines, defaults to [Dispatchers.IO]/[Dispatchers.Unconfined] (on js)
 */
@ConfigurationDSL
data class ConnectionConfiguration(
    var reconnectAttempts: Int = 3,
    var reconnectDelay: Long = 3000L,
    var poolSize: Int = 50,
    var dispatcher: CoroutineDispatcher = Dispatchers.IO_OR_UNCONFINED,
)
