package eu.vendeli.rethis.command.pubsub

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.common.Subscription
import eu.vendeli.rethis.types.common.SubscriptionType
import eu.vendeli.rethis.types.interfaces.SubscriptionHandler
import eu.vendeli.rethis.utils.registerSubscription

public suspend fun ReThis.pSubscribe(vararg pattern: String, callback: SubscriptionHandler) {
    pattern.forEach { registerSubscription(it, Subscription(SubscriptionType.PATTERN, callback)) }
}
