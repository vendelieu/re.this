package eu.vendeli.rethis.wrappers

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.commands.*
import kotlinx.coroutines.runBlocking

class ReThisSet(
    private val client: ReThis,
    private val bucket: String,
) : AbstractMutableSet<String>() {
    override val size: Int
        get() = runBlocking { client.sCard(bucket).toInt() }

    override fun add(element: String): Boolean = runBlocking {
        client.sAdd(bucket, element) > 0
    }

    override fun remove(element: String): Boolean = runBlocking {
        client.sRem(bucket, element) > 0
    }

    override fun iterator(): MutableIterator<String> = runBlocking {
        client.sMembers(bucket).toMutableSet().iterator()
    }

    override fun contains(element: String): Boolean = runBlocking {
        client.sIsMember(bucket, element)
    }

    override fun clear(): Unit = runBlocking {
        client.sPop(bucket, client.sCard(bucket)) // Pops all elements
    }

    override fun addAll(elements: Collection<String>): Boolean = runBlocking {
        client.sAdd(bucket, *elements.toTypedArray()) > 0
    }

    override fun isEmpty(): Boolean = size == 0
}

// Factory method to create a ReThisSet
@Suppress("FunctionName")
fun ReThis.Set(key: String) = ReThisSet(this, key)
