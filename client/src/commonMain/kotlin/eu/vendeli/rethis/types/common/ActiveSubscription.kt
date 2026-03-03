package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.types.interfaces.PubSubHandler
import io.ktor.util.collections.*
import kotlinx.coroutines.Job

internal data class ActiveSubscription(
    val connectionProvider: ConnectionProvider,
    val handlers: ConcurrentMap<PubSubHandler, MutableSet<Job>> = ConcurrentMap(),
)
