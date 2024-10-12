package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.types.core.SubscriptionHandler

class ChannelSubscription(
    val channel: String,
    val handler: SubscriptionHandler,
)
