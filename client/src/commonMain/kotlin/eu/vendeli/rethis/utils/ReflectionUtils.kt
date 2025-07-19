package eu.vendeli.rethis.utils

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
internal inline fun <T> Any.safeCast(): T? = this as? T
