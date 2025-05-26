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

inline fun Any.toArgument(): Argument = when (this) {
    is String -> toArgument()
    is Long -> toArgument()
    is Int -> toArgument()
    is Double -> toArgument()
    is ByteArray -> toArgument()
    is Argument -> this
    else -> toString().toArgument()
}

inline fun ByteArray.toArgument() = BaArg(this)

inline fun Double.toArgument() = DoubleArg(this)

inline fun Int.toArgument() = IntArg(this)

inline fun Long.toArgument() = LongArg(this)

fun String.toArgument() = StringArg(this)

inline fun List<Any>.toArgument(): List<Argument> = map { it.toArgument() }

inline fun Array<out String>.toArgument(): Array<Argument> = map { it.toArgument() }.toTypedArray()
