package eu.vendeli.rethis.wrappers

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.commands.*
import eu.vendeli.rethis.utils.coRunBlocking

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

    override fun iterator(): MutableIterator<String> = coRunBlocking {
        client.sMembers(bucket).toMutableSet().iterator()
    }

    override fun contains(element: String): Boolean = coRunBlocking {
        client.sIsMember(bucket, element)
    }

    override fun clear(): Unit = coRunBlocking {
        client.sPop(bucket, client.sCard(bucket)) // Pops all elements
    }

    override fun addAll(elements: Collection<String>): Boolean = coRunBlocking {
        client.sAdd(bucket, *elements.toTypedArray()) > 0
    }

    override fun isEmpty(): Boolean = size == 0
}

@Suppress("FunctionName")
fun ReThis.Set(key: String) = ReThisSet(this, key)
