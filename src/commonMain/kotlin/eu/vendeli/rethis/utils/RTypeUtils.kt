package eu.vendeli.rethis.utils

import eu.vendeli.rethis.types.core.*
import kotlin.reflect.KClass

@Suppress("NOTHING_TO_INLINE")
inline fun <T : Any> RType.unwrap(type: KClass<T>): T? =
    if (this is RType.Error) throw exception
    else unwrapType(type)

fun <T : Any> RType.unwrapType(type: KClass<T>): T? = when {
    type == RType::class -> cast(type)
    type == RPrimitive::class -> safeCast(type)
    this is PlainString -> value.safeCast(type)
    this is Int64 -> value.safeCast(type)
    this is Bool -> value.safeCast(type)
    this is F64 -> value.safeCast(type)
    this is BigNumber -> value.safeCast(type)
    this is VerbatimString -> value.safeCast(type)
    this is BulkString -> value.safeCast(type)
    else -> null
}
