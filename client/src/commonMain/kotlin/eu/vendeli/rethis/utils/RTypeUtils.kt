package eu.vendeli.rethis.utils

import com.ionspin.kotlin.bignum.integer.BigInteger
import eu.vendeli.rethis.annotations.ReThisInternal
import eu.vendeli.rethis.api.spec.common.types.BigNumber
import eu.vendeli.rethis.api.spec.common.types.Bool
import eu.vendeli.rethis.api.spec.common.types.BulkString
import eu.vendeli.rethis.api.spec.common.types.F64
import eu.vendeli.rethis.api.spec.common.types.Int64
import eu.vendeli.rethis.api.spec.common.types.PlainString
import eu.vendeli.rethis.api.spec.common.types.RArray
import eu.vendeli.rethis.api.spec.common.types.RMap
import eu.vendeli.rethis.api.spec.common.types.RPrimitive
import eu.vendeli.rethis.api.spec.common.types.RSet
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.api.spec.common.types.VerbatimString
import io.ktor.util.logging.*

fun RType.isOk() = unwrap<String>() == "OK"

inline fun RType.handleEx(): RType =
    if (this is RType.Error) throw exception else this

inline fun <reified T> RType.unwrap(): T? = run {
    handleEx()
    when {
        T::class == RType::class -> this as T
        T::class == RPrimitive::class -> this as? T
        this is PlainString -> if (T::class == String::class) value as T else null
        this is Int64 -> if (T::class == Long::class) value as T else null
        this is Bool -> if (T::class == Boolean::class) value as T else null
        this is F64 -> if (T::class == Double::class) value as T else null
        this is BigNumber -> if (T::class == BigInteger::class) value as T else null
        this is VerbatimString -> if (T::class == String::class) value as T else null
        this is BulkString -> if (T::class == String::class) value as T else null
        else -> {
            __ParserLogger.warn("Wrong unwrapping [common] method used for $this")
            null
        }
    }
}

inline fun <reified T> RType.unwrapList(): List<T> {
    handleEx()
    val response = mutableListOf<T>()
    if (this is RArray) {
        value.forEach { i ->
            i.unwrap<T>()?.let { response.add(it) }
        }
    } else {
        __ParserLogger.warn("Wrong unwrapping [list] method used for $this")
    }
    return response.toList()
}

inline fun <reified T> RType.unwrapSet(): Set<T> {
    handleEx()
    val response = mutableSetOf<T>()
    if (this is RSet) {
        value.forEach { i ->
            i.unwrap<T>()?.let { response.add(it) }
        }
    } else {
        __ParserLogger.warn("Wrong unwrapping [set] method used for $this")
    }
    return response.toSet()
}

inline fun <reified K, reified V> RType.unwrapMap(): Map<K, V?>? = run {
    handleEx()
    when (this) {
        is RMap -> value.entries.associate { (key, value) ->
            key.unwrap<K>()!! to value?.unwrap<V>()
        }

        else -> {
            __ParserLogger.warn("Wrong unwrapping [map] method used for $this")
            null
        }
    }
}

/**
 * Unwrap RESP map.
 *
 * It can be either an array with pair values from RESP 2,
 * or a map from RESP 3.
 */
inline fun <reified L, reified R> RType.unwrapRESPAgnosticMap(): Map<L, R?>? = run {
    handleEx()
    if (this is RArray) value.chunked(2).associate {
        it.first().unwrap<L>()!! to it.last().unwrap<R>()
    } else unwrapMap<L, R>()
}

private val logger = KtorSimpleLogger("eu.vendeli.rethis.RTypeParser")

@ReThisInternal
@Suppress("ObjectPropertyName")
val RType.__ParserLogger get() = logger
