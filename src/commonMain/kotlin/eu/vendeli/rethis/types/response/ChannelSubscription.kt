package eu.vendeli.rethis.types.response

import eu.vendeli.rethis.types.interfaces.SubscriptionHandler

class ChannelSubscription(
    val channel: String,
    val handler: SubscriptionHandler,
)
