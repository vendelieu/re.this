package eu.vendeli.rethis.wrappers

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.shared.request.common.FieldValue
import eu.vendeli.rethis.command.hash.*
import eu.vendeli.rethis.utils.coRunBlocking

class ReThisMap(
    private val client: ReThis,
    private val bucket: String,
) : AbstractMutableMap<String, String>() {
    override val entries: MutableSet<MutableMap.MutableEntry<String, String>>
        get() = coRunBlocking {
            client
                .hGetAll(bucket)
                .entries
                .map {
                    (it.key to it.value!!).toMapEntry(client, bucket)
                }.toMutableSet()
        }

    override val keys: MutableSet<String>
        get() = coRunBlocking {
            client.hKeys(bucket).toMutableSet()
        }

    override val size: Int
        get() = coRunBlocking {
            client.hLen(bucket).toInt()
        }
    override val values: MutableCollection<String>
        get() = coRunBlocking {
            client.hVals(bucket).toMutableList()
        }

    override fun put(key: String, value: String): String = updateValue(client, bucket, key to value).let { value }

    override fun get(key: String): String? = coRunBlocking {
        client.hGet(bucket, key)
    }

    override fun containsKey(key: String): Boolean = coRunBlocking {
        client.hExists(bucket, key)
    }

    override fun clear(): Unit = coRunBlocking {
        client.hDel(bucket, *keys.toTypedArray())
    }

    override fun isEmpty(): Boolean = size == 0

    override fun putAll(from: Map<out String, String>): Unit = coRunBlocking {
        client.hSet(bucket, *from.entries.map { FieldValue(it.key, it.value) }.toTypedArray())
    }

    override fun remove(key: String): String? = coRunBlocking {
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

    override fun setValue(newValue: String): String = updateValue(client, bucket, key to newValue).let { newValue }
}

@Suppress("NOTHING_TO_INLINE")
private inline fun updateValue(client: ReThis, bucket: String, pair: Pair<String, String>) = coRunBlocking {
    client.hMSet(bucket, FieldValue(pair.first, pair.second)).let { pair.second }
}
