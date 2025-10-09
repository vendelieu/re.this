package eu.vendeli.rethis.core

import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.types.common.Subscription
import eu.vendeli.rethis.types.common.SubscriptionWorker
import eu.vendeli.rethis.types.interfaces.SubscriptionEventHandler
import io.ktor.util.collections.*

class SubscriptionManager {
    internal val subscriptionJobs = ConcurrentMap<String, MutableSet<SubscriptionWorker>>()
    internal val subscriptionsHandlers = mutableMapOf<String, MutableSet<Subscription>>()
    internal var eventHandler: SubscriptionEventHandler? = null

    val size
        get() = subscriptionsHandlers.values.fold(0) { i, j -> i + j.size }

    fun registerSubscription(id: String, subscription: Subscription) {
        subscriptionsHandlers.getOrPut(id) { mutableSetOf() }.add(subscription)
    }

    fun unsubscribe(id: String): Boolean {
        subscriptionJobs.remove(id)?.forEach { it.job.cancel() } // remove job and cancel it

        subscriptionsHandlers.remove(id)

        return !subscriptionsHandlers.contains(id)
    }

    fun unsubscribeAll(): Boolean {
        subscriptionsHandlers.toMap().forEach { unsubscribe(it.key) }
        return subscriptionsHandlers.isEmpty()
    }

    fun setEventHandler(eventHandler: SubscriptionEventHandler) {
        this.eventHandler = eventHandler
    }

    internal fun isHandlerRegistered(id: String, provider: ConnectionProvider): Boolean =
        subscriptionJobs.containsKey(id) && subscriptionJobs[id]?.any { it.connectionProvider == provider } == true

    internal fun registerHandler(id: String, worker: SubscriptionWorker) {
        subscriptionJobs.getOrPut(id) { mutableSetOf() }.add(worker)
    }

    internal fun unregisterHandler(id: String, subscription: Subscription) {
        subscriptionsHandlers[id]?.remove(subscription)
    }
}
