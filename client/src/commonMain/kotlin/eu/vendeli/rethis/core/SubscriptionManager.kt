package eu.vendeli.rethis.core

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.types.common.ActiveSubscription
import eu.vendeli.rethis.types.common.SubscribeTarget
import eu.vendeli.rethis.types.interfaces.ExperimentalReThisMetricsApi
import eu.vendeli.rethis.types.interfaces.PubSubHandler
import io.ktor.util.collections.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex

@OptIn(ExperimentalReThisMetricsApi::class)
class SubscriptionManager internal constructor(
    internal val client: ReThis,
) {
    internal val activeSubscriptions = ConcurrentMap<SubscribeTarget, ActiveSubscription>()
    internal val globalHandlers = ConcurrentSet<PubSubHandler>()

    // Guards the register/unregister read-modify-write sequences; critical sections are short map operations.
    private val registryGuard = Mutex()

    private inline fun <T> guarded(block: () -> T): T {
        while (!registryGuard.tryLock()) {
            // spin: the guarded sections never suspend or block
        }
        try {
            return block()
        } finally {
            registryGuard.unlock()
        }
    }

    val size: Int get() = activeSubscriptions.size

    fun unsubscribe(target: SubscribeTarget) {
        val removed = guarded { activeSubscriptions.remove(target) } ?: return
        removed.handlers.values
            .flatten()
            .forEach { it.cancel() }
        recordTargetChange(target, delta = -1)
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
        var isTargetCreated = false
        guarded {
            activeSubscriptions
                .getOrPut(target) {
                    isTargetCreated = true
                    ActiveSubscription(provider)
                }.handlers
                .getOrPut(handler) { ConcurrentSet() }
                .add(worker)
        }
        if (isTargetCreated) recordTargetChange(target, delta = 1)
    }

    // the active-subscription gauge counts targets (not workers): +1 when a target is created, -1 when it is removed
    private fun recordTargetChange(target: SubscribeTarget, delta: Int) {
        client.cfg.metricsRecorder?.onSubscriptionChange(target::class.simpleName ?: "Subscribe", delta)
    }

    internal fun unregisterHandler(
        target: SubscribeTarget,
        handler: PubSubHandler,
        worker: Job,
    ) {
        val isTargetRemoved = guarded {
            val subscription = activeSubscriptions[target] ?: return
            val jobs = subscription.handlers[handler] ?: return
            jobs.remove(worker)
            if (jobs.isNotEmpty()) return
            subscription.handlers.remove(handler)
            if (subscription.handlers.isNotEmpty()) return
            activeSubscriptions.remove(target)
            true
        }
        if (isTargetRemoved) recordTargetChange(target, delta = -1)
    }
}
