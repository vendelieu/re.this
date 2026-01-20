package eu.vendeli.rethis.core

import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.types.common.ActiveSubscription
import eu.vendeli.rethis.types.common.SubscribeTarget
import eu.vendeli.rethis.types.interfaces.PubSubHandler
import io.ktor.util.collections.*
import kotlinx.coroutines.Job

class SubscriptionManager {
    internal val activeSubscriptions = ConcurrentMap<SubscribeTarget, ActiveSubscription>()
    internal val globalHandlers = mutableSetOf<PubSubHandler>()

    fun unsubscribe(target: SubscribeTarget) {
        val subscriptionToRemove = activeSubscriptions[target]
        subscriptionToRemove?.handlers?.forEach { (_, jobs) ->
            jobs.forEach { job -> job.cancel() }
        }
        subscriptionToRemove?.handlers?.clear()
        activeSubscriptions.remove(target)
    }

    fun unsubscribeAll() {
        activeSubscriptions.keys.forEach { unsubscribe(it) }
    }

    fun registerGlobalHandler(handler: PubSubHandler) {
        globalHandlers.add(handler)
    }

    fun unregisterGlobalHandler(handler: PubSubHandler) {
        globalHandlers.remove(handler)
    }

    fun isActiveHandlers(target: SubscribeTarget) = activeSubscriptions[target]?.handlers?.isNotEmpty() ?: false

    internal fun registerSubscription(
        target: SubscribeTarget,
        provider: ConnectionProvider,
        handler: PubSubHandler,
        worker: Job,
    ) {
        activeSubscriptions
            .getOrPut(target) {
                ActiveSubscription(provider, mutableMapOf())
            }.handlers
            .getOrPut(handler) { mutableSetOf() }
            .add(worker)
    }

    internal fun unregisterHandler(
        target: SubscribeTarget,
        handler: PubSubHandler,
        worker: Job,
    ) {
        activeSubscriptions[target]?.handlers?.get(handler)?.remove(worker)
    }
}
