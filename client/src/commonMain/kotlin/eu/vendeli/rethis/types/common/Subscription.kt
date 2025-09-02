package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.types.interfaces.SubscriptionHandler

data class Subscription(
    val type: SubscriptionType,
    val handler: SubscriptionHandler,
)
