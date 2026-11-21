package eu.vendeli.rethis.wrappers

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.command.generic.del
import eu.vendeli.rethis.command.set.*
import eu.vendeli.rethis.utils.coRunBlocking

/**
 * Blocking set wrapper over a Redis set.
 *
 * Not available on JS and wasmJs: every call throws [UnsupportedOperationException]; use the suspend API there.
 */
class ReThisSet(
    private val client: ReThis,
    private val bucket: String,
) : AbstractMutableSet<String>() {
    override val size: Int
        get() = coRunBlocking { client.sCard(bucket).toInt() }

    override fun add(element: String): Boolean = coRunBlocking {
        client.sAdd(bucket, element) > 0
    }

    override fun remove(element: String): Boolean = coRunBlocking {
        client.sRem(bucket, element) > 0
    }

    override fun iterator(): MutableIterator<String> {
        val snapshot = coRunBlocking { client.sMembers(bucket).toList() }.iterator()
        return object : MutableIterator<String> {
            private var last: String? = null

            override fun hasNext(): Boolean = snapshot.hasNext()

            override fun next(): String = snapshot.next().also { last = it }

            override fun remove() {
                val element = checkNotNull(last) { "next() must be called before remove()" }
                coRunBlocking { client.sRem(bucket, element) }
                last = null
            }
        }
    }

    override fun contains(element: String): Boolean = coRunBlocking {
        client.sIsMember(bucket, element)
    }

    override fun clear(): Unit = coRunBlocking {
        client.del(bucket)
    }

    override fun addAll(elements: Collection<String>): Boolean = coRunBlocking {
        if (elements.isEmpty()) return@coRunBlocking false
        client.sAdd(bucket, *elements.toTypedArray()) > 0
    }

    override fun isEmpty(): Boolean = size == 0
}

/**
 * Creates a [ReThisSet] over the set at [key]. Not available on JS and wasmJs.
 */
@Suppress("FunctionName")
fun ReThis.Set(key: String) = ReThisSet(this, key)
