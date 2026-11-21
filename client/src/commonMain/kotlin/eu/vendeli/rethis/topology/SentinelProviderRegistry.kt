package eu.vendeli.rethis.topology

import eu.vendeli.rethis.providers.ConnectionProvider
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Lock-free list of the providers opened for sentinel subscriptions. Once [closeAll] ran, a provider added later is
 * closed right away instead of being kept, so a subscription racing with shutdown cannot leak its connection.
 */
@OptIn(ExperimentalAtomicApi::class)
internal class SentinelProviderRegistry {
    private class State(
        val closed: Boolean,
        val providers: List<ConnectionProvider>,
    )

    private val state = AtomicReference(State(closed = false, providers = emptyList()))

    val providers: List<ConnectionProvider> get() = state.load().providers

    /** Stores [provider]; returns false (after closing it) when the registry is already closed. */
    fun add(provider: ConnectionProvider): Boolean {
        while (true) {
            val current = state.load()
            if (current.closed) {
                provider.close()
                return false
            }
            if (state.compareAndSet(current, State(closed = false, providers = current.providers + provider))) {
                return true
            }
        }
    }

    fun closeAll() {
        while (true) {
            val current = state.load()
            if (state.compareAndSet(current, State(closed = true, providers = emptyList()))) {
                current.providers.forEach { it.close() }
                return
            }
        }
    }
}
