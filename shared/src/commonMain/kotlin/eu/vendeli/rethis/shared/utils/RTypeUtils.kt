package eu.vendeli.rethis.shared.utils

import com.ionspin.kotlin.bignum.integer.BigInteger
import eu.vendeli.rethis.shared.types.*
import io.ktor.util.logging.*
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.*
import kotlinx.io.Buffer

fun Buffer.readResponseWrapped(
    charset: Charset = Charsets.UTF_8,
    rawOnly: Boolean = false,
    code: RespCode? = null,
): RType {
    // Peek first byte for type
    if (remaining == 0L) return RType.Null
    val code = code ?: RespCode.fromCode(readByte())

    return when (code) {
        RespCode.ARRAY -> {
            val size = readLineStrict().toInt()
            if (size < 0) RType.Null else {
                val list = mutableListOf<RType>()
                repeat(size) { list += readResponseWrapped(charset, rawOnly) }
                RArray(list)
            }
        }

        RespCode.SET -> {
            val size = readLineStrict().toInt()
            if (size < 0) RType.Null else {
                val set = mutableSetOf<RPrimitive>()
                repeat(size) { set += readResponseWrapped(charset, rawOnly) as RPrimitive }
                RSet(set)
            }
        }

        RespCode.PUSH -> {
            val size = readLineStrict().toInt()
            if (size < 0) RType.Null else {
                val list = mutableListOf<RPrimitive>()
                repeat(size) { list += readResponseWrapped(charset, rawOnly) as RPrimitive }
                Push(list)
            }
        }

        RespCode.MAP -> {
            val size = readLineStrict().toInt()
            if (size < 0) RType.Null else {
                val map = mutableMapOf<RPrimitive, RType>()
                repeat(size) {
                    val key = readResponseWrapped(charset, rawOnly) as RPrimitive
                    val value = readResponseWrapped(charset, rawOnly)
                    map[key] = value
                }
                RMap(map)
            }
        }

        else -> readSimpleResponseWrapped(charset, rawOnly, code)
    }
}

private const val TRUE_BYTE = 't'.code.toByte()
private const val FALSE_BYTE = 'f'.code.toByte()

@OptIn(InternalIoApi::class)
private fun Buffer.readSimpleResponseWrapped(
    charset: Charset,
    rawOnly: Boolean,
    code: RespCode? = null,
): RType {
    val code = code ?: RespCode.fromCode(readByte())
    if (rawOnly) {
        readLineCRLF() // skip size
        return RType.Raw(readByteArray())
    }

    return when (code) {
        RespCode.SIMPLE_STRING -> PlainString(readPartLine(charset))
        RespCode.SIMPLE_ERROR -> RType.Error(readPartLine(charset))
        RespCode.INTEGER -> Int64(readLineCRLF().readDecimalLong())
        RespCode.BOOLEAN -> when (readByte()) {
            TRUE_BYTE -> Bool(true)
            FALSE_BYTE -> Bool(false)
            else -> throw ResponseParsingException("Invalid boolean format")
        }.also { readLineCRLF() }

        RespCode.DOUBLE -> F64(readLineStrict().toDouble())
        RespCode.BIG_NUMBER -> try {
            BigNumber(BigInteger.parseString(readPartLine(charset)))
        } catch (e: NumberFormatException) {
            throw ResponseParsingException("Invalid BigInteger format", e)
        }

        RespCode.BULK -> {
            val size = readLineStrict().toInt()
            if (size < 0) return RType.Null
            else if (size == 0) {
                readLineCRLF() // skip crlf
                return RType.Null
            }
            val content = readPartLine(charset)
            BulkString(content)
        }

        RespCode.BULK_ERROR -> {
            readLineCRLF() // skip size
            RType.Error(readPartLine(charset))
        }

        RespCode.VERBATIM_STRING -> {
            val size = readLineStrict().toInt()
            if (size < 0) return RType.Null
            val encoding = readByteArray(3).decodeToString()
            readByte() // skip ':' byte
            val content = readPartLine(charset)
            VerbatimString(encoding, content)
        }

        RespCode.NULL -> RType.Null
        else -> throw ResponseParsingException("Unexpected simple code: $code")
    }
}

private fun Buffer.readPartLine(charset: Charset) = readLineCRLF().readText(charset)

internal const val CARRIAGE_RETURN_BYTE = '\r'.code.toByte()
internal const val NEWLINE_BYTE = '\n'.code.toByte()

private inline fun Buffer.readLineCRLF(): Buffer {
    val buffer = Buffer()
    while (true) {
        val byte = readByte()

        if (byte == CARRIAGE_RETURN_BYTE) {
            val nextByte = readByte()
            if (nextByte == NEWLINE_BYTE) {
                break
            } else {
                buffer.writeByte(CARRIAGE_RETURN_BYTE)
                buffer.writeByte(nextByte)
                continue
            }
        }
        buffer.writeByte(byte)
    }
    return buffer
}

fun RType.isOk() = unwrap<String>() == "OK"

inline fun RType.handleEx(): RType =
    if (this is RType.Error) throw exception else this

inline fun <reified T> RType.unwrap(): T? {
    handleEx()
    return when {
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
    when (this) {
        is RArray -> {
            value.forEach { i -> i.unwrap<T>()?.let { response.add(it) } }
        }

        is Push -> {
            value.forEach { i -> i.unwrap<T>()?.let { response.add(it) } }
        }

        else -> {
            __ParserLogger.warn("Wrong unwrapping [list] method used for $this")
        }
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

@Suppress("ObjectPropertyName")
val RType.__ParserLogger get() = logger
