package eu.vendeli.rethis.command.pubsub

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.common.Subscription
import eu.vendeli.rethis.types.common.SubscriptionType
import eu.vendeli.rethis.types.interfaces.SubscriptionHandler
import eu.vendeli.rethis.utils.registerSubscription

public suspend fun ReThis.subscribe(vararg channel: String, callback: SubscriptionHandler) {
    channel.forEach { registerSubscription(it, Subscription(SubscriptionType.PLAIN, callback)) }
}
