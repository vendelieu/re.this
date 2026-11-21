package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.annotations.ReThisInternal
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.types.interfaces.PubSubHandler
import eu.vendeli.rethis.utils.registerSubscription
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration

/**
 * Waits for a lock release notification published on [channelName].
 *
 * One instance owns exactly one subscription worker, created by [open] and cancelled by [close] through the
 * job returned at registration, so channel subscriptions held by other waiters stay untouched. A missed
 * notification is always recovered by the bounded timeout passed to [await]. If [open] fails the waiter
 * degrades to timed polling for the rest of the call.
 */
@OptIn(ExperimentalAtomicApi::class, ReThisInternal::class)
internal class LockReleaseWaiter(
    private val client: ReThis,
    private val channelName: String,
) {
    private val signal = Channel<Unit>(Channel.CONFLATED)
    private val closed = AtomicBoolean(false)
    private val worker = AtomicReference<Job?>(null)
    private val target = SubscribeTarget.Channel(channelName)

    private val handler = object : PubSubHandler {
        override suspend fun onSubscribe(kind: PubSubKind, target: SubscribeTarget, subscribedChannels: Long) =
            cancelSelfIfClosed()

        override suspend fun onUnsubscribe(kind: PubSubKind, target: SubscribeTarget, subscribedChannels: Long) = Unit

        override suspend fun onMessage(kind: PubSubKind, channel: String, message: RType, pattern: String?) {
            cancelSelfIfClosed()
            signal.trySend(Unit)
        }

        // The subscription is gone; the timeout passed to await keeps the waiter correct without it.
        override suspend fun onException(target: SubscribeTarget, ex: Exception) = Unit

        private suspend fun cancelSelfIfClosed() {
            if (closed.load()) currentCoroutineContext()[Job]?.cancel()
        }
    }

    /**
     * Registers the subscription worker. Registration is not cancellable, so a connection borrowed for the
     * worker is always handed to it and the returned job is always kept for [close].
     *
     * It returns once the worker is launched, not once the server processed SUBSCRIBE; a release published
     * in that gap is recovered by the timeout passed to [await].
     */
    suspend fun open() {
        try {
            withContext(NonCancellable) {
                worker.store(client.subscriptions.registerSubscription(target, handler))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            client.logger.warn("Unable to subscribe to $channelName, falling back to timed retries", e)
        }
    }

    /** Suspends until a release notification arrives or [timeout] elapses. */
    suspend fun await(timeout: Duration) {
        withTimeoutOrNull(timeout) { signal.receive() }
    }

    /** Releases the subscription owned by this waiter. Safe to call repeatedly and when never opened. */
    fun close() {
        closed.store(true)
        signal.close()
        val job = worker.exchange(null) ?: return
        if (isStarted(job)) {
            job.cancel()
        } else {
            // A worker cancelled before its body starts never disposes the connection it was given, so
            // let it start (it registers itself first) and cancel it then.
            client.scope.launch {
                while (job.isActive && !isStarted(job)) delay(START_POLL_MS)
                job.cancel()
            }
        }
    }

    private fun isStarted(job: Job): Boolean =
        client.subscriptions.activeSubscriptions[target]
            ?.handlers
            ?.get(handler)
            ?.contains(job) == true

    private companion object {
        const val START_POLL_MS = 1L
    }
}
