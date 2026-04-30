package eu.vendeli.rethis.core

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.types.common.ActiveSubscription
import eu.vendeli.rethis.types.common.SubscribeTarget
import eu.vendeli.rethis.types.interfaces.PubSubHandler
import io.ktor.util.collections.*
import kotlinx.coroutines.Job

class SubscriptionManager internal constructor(
    internal val client: ReThis,
) {
    internal val activeSubscriptions = ConcurrentMap<SubscribeTarget, ActiveSubscription>()
    internal val globalHandlers = ConcurrentSet<PubSubHandler>()

    val size: Int get() = activeSubscriptions.size

    fun unsubscribe(target: SubscribeTarget) {
        val removed = activeSubscriptions.remove(target) ?: return
        removed.handlers.values
            .flatten()
            .forEach { it.cancel() }
    }

    fun unsubscribeAll() {
        activeSubscriptions.keys.toList().forEach { unsubscribe(it) }
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
                ActiveSubscription(provider)
            }.handlers
            .getOrPut(handler) { ConcurrentSet() }
            .add(worker)
    }

    internal fun unregisterHandler(
        target: SubscribeTarget,
        handler: PubSubHandler,
        worker: Job,
    ) {
        val subscription = activeSubscriptions[target] ?: return
        val jobs = subscription.handlers[handler] ?: return
        jobs.remove(worker)
        if (jobs.isEmpty()) {
            subscription.handlers.remove(handler)
            if (subscription.handlers.isEmpty()) {
                activeSubscriptions.remove(target)
            }
        }
    }
}
