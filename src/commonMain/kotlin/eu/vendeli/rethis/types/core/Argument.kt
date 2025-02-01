package eu.vendeli.rethis.types.core

import kotlin.jvm.JvmInline

interface VaryingArgument {
    val data: List<Argument>
}

interface Argument

fun Any.toArgument(): Argument = when (this) {
    is String -> toArg()
    is Long -> toArg()
    is Int -> toArg()
    is Double -> toArg()
    is ByteArray -> toArg()
    is Argument -> this
    else -> toString().toArg()
}

@Suppress("NOTHING_TO_INLINE")
inline fun List<Any>.toArgument(): List<Argument> = map { it.toArgument() }

@Suppress("NOTHING_TO_INLINE")
inline fun Array<out String>.toArgument(): Array<Argument> = map { it.toArgument() }.toTypedArray()

@JvmInline
internal value class StringArg(
    val value: String,
) : Argument

@Suppress("NOTHING_TO_INLINE")
internal inline fun String.toArg() = StringArg(this)

@JvmInline
value class LongArg(
    val value: Long,
) : Argument

@Suppress("NOTHING_TO_INLINE")
internal inline fun Long.toArg() = LongArg(this)

@JvmInline
value class IntArg(
    val value: Int,
) : Argument

@Suppress("NOTHING_TO_INLINE")
internal inline fun Int.toArg() = IntArg(this)

@JvmInline
value class DoubleArg(
    val value: Double,
) : Argument

@Suppress("NOTHING_TO_INLINE")
internal inline fun Double.toArg() = DoubleArg(this)

@JvmInline
value class BaArg(
    val value: ByteArray,
) : Argument

@Suppress("NOTHING_TO_INLINE")
internal inline fun ByteArray.toArg() = BaArg(this)
