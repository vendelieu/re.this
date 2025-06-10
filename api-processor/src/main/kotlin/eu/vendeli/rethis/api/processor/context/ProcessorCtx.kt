package eu.vendeli.rethis.api.processor.context

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

internal class ProcessorContext {
    private val storage: ConcurrentMap<ContextKey<*>, ContextElement> = ConcurrentHashMap()

    fun <E : ContextElement> put(element: E) {
        storage[element.key] = element
    }

    @Suppress("UNCHECKED_CAST")
    fun <E : ContextElement> remove(key: ContextKey<E>) {
        storage.remove(key)
    }

    fun clearPerCommand() {
        storage.keys
            .filter { it.isPerCommand }
            .forEach { storage.remove(it) }
    }

    fun clearAll() {
        storage.clear()
    }

    operator fun plusAssign(element: ContextElement) = put(element)

    operator fun minusAssign(element: ContextElement) = remove(element.key)

    @Suppress("UNCHECKED_CAST")
    operator fun <E : ContextElement> get(key: ContextKey<E>): E? = storage[key] as? E
}
