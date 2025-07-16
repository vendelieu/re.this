package eu.vendeli.rethis.core

import eu.vendeli.rethis.types.common.Subscription
import eu.vendeli.rethis.types.interfaces.SubscriptionEventHandler
import io.ktor.util.collections.*
import kotlinx.coroutines.Job

class SubscriptionManager {
    internal val subscriptionJobs = ConcurrentMap<String, Job>()
    internal val subscriptionsHandlers = mutableMapOf<String, MutableSet<Subscription>>()
    internal var eventHandler: SubscriptionEventHandler? = null

    val size
        get() = subscriptionsHandlers.values.fold(0) { i, j -> i + j.size }

    fun registerSubscription(id: String, subscription: Subscription) {
        subscriptionsHandlers.getOrPut(id) { mutableSetOf() }.add(subscription)
    }

    fun unsubscribe(id: String): Boolean {
        subscriptionJobs.remove(id)?.cancel() // remove job and cancel it

        subscriptionsHandlers.remove(id)

        return !subscriptionsHandlers.contains(id)
    }

    fun unsubscribeAll(): Boolean {
        subscriptionsHandlers.forEach { unsubscribe(it.key) }
        return subscriptionsHandlers.isEmpty()
    }

    fun setEventHandler(eventHandler: SubscriptionEventHandler) {
        this.eventHandler = eventHandler
    }

    internal fun registerHandler(id: String, job: Job) {
        subscriptionJobs.computeIfAbsent(id) { job }
    }

    internal fun unregisterHandler(id: String, subscription: Subscription) {
        subscriptionsHandlers[id]?.remove(subscription)
    }
}
