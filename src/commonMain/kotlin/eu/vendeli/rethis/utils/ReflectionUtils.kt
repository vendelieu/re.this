package eu.vendeli.rethis.utils

import io.ktor.util.reflect.TypeInfo
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
internal inline fun <T> Any.cast(): T = this as T

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
internal inline fun <T : Any> Any.cast(clazz: KClass<T>): T = this as T

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
internal inline fun <T> Any.safeCast(): T? = this as? T

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
internal inline fun <T : Any> Any.safeCast(clazz: KClass<T>): T? =
    if (this::class == clazz) this as T else null

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
internal inline fun <T : Any> Any.safeCast(typeInfo: TypeInfo): T? =
    if (typeInfo.type.isInstance(this)) this as T else null
