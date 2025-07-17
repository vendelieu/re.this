package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.providers.ConnectionProvider
import kotlinx.coroutines.Job

internal data class SubscriptionWorker(
    val connectionProvider: ConnectionProvider,
    val job: Job,
)
