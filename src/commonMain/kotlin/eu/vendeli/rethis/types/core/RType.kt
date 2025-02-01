package eu.vendeli.rethis.types.core

import com.ionspin.kotlin.bignum.integer.BigInteger
import eu.vendeli.rethis.RedisError
import io.ktor.util.logging.*

private val logger = KtorSimpleLogger("eu.vendeli.rethis.InputParser")

sealed class RType {
    open val value: Any? get() = null

    data object Null : RType()
    data class Error(
        val exception: RedisError,
    ) : RType() {
        constructor(message: String) : this(RedisError(message))
    }

    class Raw internal constructor(
        override val value: ByteArray,
    ) : RType()
}

sealed class RPrimitive : RType()

data class PlainString(
    override val value: String,
) : RPrimitive()

data class Int64(
    override val value: Long,
) : RPrimitive()

data class Bool(
    override val value: Boolean,
) : RPrimitive()

data class F64(
    override val value: Double,
) : RPrimitive()

data class BigNumber(
    override val value: BigInteger,
) : RPrimitive()

data class VerbatimString(
    val encoding: String,
    val data: String,
) : RPrimitive() {
    override val value = "$encoding:$data"
}

data class BulkString(
    override val value: String,
) : RPrimitive()

data class RArray(
    override val value: List<RType>,
) : RType()

data class RMap(
    override val value: Map<RPrimitive, RType?>,
) : RType()

data class RSet(
    override val value: Set<RPrimitive>,
) : RType()

data class Push(
    override val value: List<RPrimitive>,
) : RType()

@Suppress("NOTHING_TO_INLINE")
internal inline fun RType.handleEx(): RType =
    if (this is RType.Error) throw exception else this

internal inline fun <reified T> RType.unwrap(): T? = handleEx().run {
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
            logger.debug("Wrong unwrapping [common] method used for $this")
            null
        }
    }
}

internal inline fun <reified T> RType.unwrapList(): List<T> {
    handleEx()
    val response = mutableListOf<T>()
    if (this is RArray) {
        value.forEach { i ->
            i.unwrap<T>()?.let { response.add(it) }
        }
    } else {
        logger.debug("Wrong unwrapping [list] method used for $this")
    }
    return response.toList()
}

internal inline fun <reified T> RType.unwrapSet(): Set<T> {
    handleEx()
    val response = mutableSetOf<T>()
    if (this is RSet) {
        value.forEach { i ->
            i.unwrap<T>()?.let { response.add(it) }
        }
    } else {
        logger.debug("Wrong unwrapping [set] method used for $this")
    }
    return response.toSet()
}

internal inline fun <reified K, reified V> RType.unwrapMap(): Map<K, V?>? =
    handleEx().run {
        when (this) {
            is RMap -> value.entries.associate { (key, value) ->
                key.unwrap<K>()!! to value?.unwrap<V>()
            }

            else -> null
        }
    }
