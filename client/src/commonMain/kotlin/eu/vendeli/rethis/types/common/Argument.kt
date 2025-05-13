package eu.vendeli.rethis.types.common

import kotlin.jvm.JvmInline

interface VaryingArgument {
    val data: List<Argument>
}

interface Argument

@JvmInline
value class StringArg(
    val value: String,
) : Argument

@JvmInline
value class LongArg(
    val value: Long,
) : Argument

@JvmInline
value class IntArg(
    val value: Int,
) : Argument

@JvmInline
value class DoubleArg(
    val value: Double,
) : Argument

@JvmInline
value class BaArg(
    val value: ByteArray,
) : Argument

@Suppress("NOTHING_TO_INLINE")
inline fun Any.toArgument(): Argument = when (this) {
    is String -> toArgument()
    is Long -> toArgument()
    is Int -> toArgument()
    is Double -> toArgument()
    is ByteArray -> toArgument()
    is Argument -> this
    else -> toString().toArgument()
}

@Suppress("NOTHING_TO_INLINE")
inline fun ByteArray.toArgument() = BaArg(this)

@Suppress("NOTHING_TO_INLINE")
inline fun Double.toArgument() = DoubleArg(this)

@Suppress("NOTHING_TO_INLINE")
inline fun Int.toArgument() = IntArg(this)

@Suppress("NOTHING_TO_INLINE")
inline fun Long.toArgument() = LongArg(this)

@Suppress("NOTHING_TO_INLINE")
fun String.toArgument() = StringArg(this)

@Suppress("NOTHING_TO_INLINE")
inline fun List<Any>.toArgument(): List<Argument> = map { it.toArgument() }

@Suppress("NOTHING_TO_INLINE")
inline fun Array<out String>.toArgument(): Array<Argument> = map { it.toArgument() }.toTypedArray()
