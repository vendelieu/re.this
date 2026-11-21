package eu.vendeli.rethis.wrappers

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.command.generic.del
import eu.vendeli.rethis.command.hash.*
import eu.vendeli.rethis.command.scripting.eval
import eu.vendeli.rethis.shared.request.common.FieldValue
import eu.vendeli.rethis.shared.types.BulkString
import eu.vendeli.rethis.utils.coRunBlocking
import kotlinx.io.readString

/**
 * Blocking map wrapper over a Redis hash.
 *
 * Not available on JS and wasmJs: every call throws [UnsupportedOperationException]; use the suspend API there.
 */
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

    override fun put(key: String, value: String): String? = updateValue(client, bucket, key to value)

    override fun get(key: String): String? = coRunBlocking {
        client.hGet(bucket, key)
    }

    override fun containsKey(key: String): Boolean = coRunBlocking {
        client.hExists(bucket, key)
    }

    override fun clear(): Unit = coRunBlocking {
        client.del(bucket)
    }

    override fun isEmpty(): Boolean = size == 0

    override fun putAll(from: Map<out String, String>): Unit = coRunBlocking {
        if (from.isEmpty()) return@coRunBlocking
        client.hSet(bucket, *from.entries.map { FieldValue(it.key, it.value) }.toTypedArray())
    }

    override fun remove(key: String): String? = coRunBlocking {
        // HGETDEL needs Redis 8.0+; a script keeps the read-and-delete atomic on every server version
        client
            .eval(REMOVE_SCRIPT, bucket, arg = listOf(key))
            .let { it as? BulkString }
            ?.value
            ?.readString()
    }

    override fun containsValue(value: String): Boolean = values.contains(value)
}

/**
 * Creates a [ReThisMap] over the hash at [key]. Not available on JS and wasmJs.
 */
@Suppress("FunctionName")
fun ReThis.Hash(key: String) = ReThisMap(this, key)

@Suppress("NOTHING_TO_INLINE")
private inline fun Pair<String, String>.toMapEntry(
    client: ReThis,
    bucket: String,
): MutableMap.MutableEntry<String, String> = object : MutableMap.MutableEntry<String, String> {
    override val key: String get() = first
    override val value: String get() = second

    // The entry came from a snapshot, so if the field vanished meanwhile the snapshot value is the previous one.
    override fun setValue(newValue: String): String = updateValue(client, bucket, first to newValue) ?: second
}

@Suppress("NOTHING_TO_INLINE")
private inline fun updateValue(client: ReThis, bucket: String, pair: Pair<String, String>): String? = coRunBlocking {
    // HGET then HSET: not atomic, a concurrent writer can change the field in between.
    val previous = client.hGet(bucket, pair.first)
    client.hMSet(bucket, FieldValue(pair.first, pair.second))
    previous
}

private const val REMOVE_SCRIPT =
    "local v=redis.call('HGET',KEYS[1],ARGV[1]); redis.call('HDEL',KEYS[1],ARGV[1]); return v"
