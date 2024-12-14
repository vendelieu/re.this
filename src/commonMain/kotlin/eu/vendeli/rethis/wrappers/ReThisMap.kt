package eu.vendeli.rethis.wrappers

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.ReThisException
import eu.vendeli.rethis.commands.*
import kotlinx.coroutines.runBlocking

class ReThisMap(
    private val client: ReThis,
    private val bucket: String,
) : AbstractMutableMap<String, String>() {
    override val entries: MutableSet<MutableMap.MutableEntry<String, String>>
        get() = runBlocking {
            client
                .hGetAll(bucket)
                ?.entries
                ?.map {
                    (it.key to it.value!!).toMapEntry(client, bucket)
                }?.toMutableSet() ?: mutableSetOf()
        }

    override val keys: MutableSet<String>
        get() = runBlocking {
            client.hKeys(bucket).toMutableSet()
        }

    override val size: Int
        get() = runBlocking {
            client.hLen(bucket).toInt()
        }
    override val values: MutableCollection<String>
        get() = runBlocking {
            client.hVals(bucket).toMutableList()
        }

    override fun put(key: String, value: String): String = updateValue(client, bucket, key to value)

    override fun get(key: String): String? = runBlocking {
        client.hGet(bucket, key)
    }

    override fun containsKey(key: String): Boolean = runBlocking {
        client.hExists(bucket, key)
    }

    override fun clear(): Unit = runBlocking {
        client.hDel(bucket, *keys.toTypedArray())
    }

    override fun isEmpty(): Boolean = size == 0

    override fun putAll(from: Map<out String, String>): Unit = runBlocking {
        client.hSet(bucket, *from.entries.map { it.toPair() }.toTypedArray())
    }

    override fun remove(key: String): String? = runBlocking {
        client.hDel(bucket, key)
        null
    }

    override fun containsValue(value: String): Boolean = values.contains(value)
}

@Suppress("FunctionName")
fun ReThis.Hash(key: String) = ReThisMap(this, key)

@Suppress("NOTHING_TO_INLINE")
private inline fun Pair<String, String>.toMapEntry(
    client: ReThis,
    bucket: String,
): MutableMap.MutableEntry<String, String> = object : MutableMap.MutableEntry<String, String> {
    override val key: String get() = first
    override val value: String get() = second

    override fun setValue(newValue: String): String = updateValue(client, bucket, key to newValue)
}

@Suppress("NOTHING_TO_INLINE")
private inline fun updateValue(client: ReThis, bucket: String, pair: Pair<String, String>) = runBlocking {
    client.hMSet(bucket, pair) ?: throw ReThisException("Error occurred while updating entry")
}