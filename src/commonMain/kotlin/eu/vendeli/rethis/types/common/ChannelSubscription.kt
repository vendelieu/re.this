package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.types.core.ReThisExceptionHandler
import eu.vendeli.rethis.types.core.SubscriptionHandler

class ChannelSubscription(
    val channel: String,
    val exceptionHandler: ReThisExceptionHandler? = null,
    val handler: SubscriptionHandler,
)
