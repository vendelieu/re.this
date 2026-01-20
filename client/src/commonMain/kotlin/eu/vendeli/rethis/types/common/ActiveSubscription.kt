package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.types.interfaces.PubSubHandler
import kotlinx.coroutines.Job

internal data class ActiveSubscription(
    val connectionProvider: ConnectionProvider,
    val handlers: MutableMap<PubSubHandler, MutableSet<Job>>,
)
