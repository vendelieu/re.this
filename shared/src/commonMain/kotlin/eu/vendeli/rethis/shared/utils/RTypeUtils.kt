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
            val size = readDecimalCrlf().toInt()
            if (size < 0) RType.Null else {
                val list = mutableListOf<RType>()
                repeat(size) { list += readResponseWrapped(charset, rawOnly) }
                RArray(list)
            }
        }

        RespCode.SET -> {
            val size = readDecimalCrlf().toInt()
            if (size < 0) RType.Null else {
                val set = mutableSetOf<RType>()
                repeat(size) { set += readResponseWrapped(charset, rawOnly) }
                RSet(set)
            }
        }

        RespCode.PUSH -> {
            val size = readDecimalCrlf().toInt()
            if (size < 0) RType.Null else {
                val list = mutableListOf<RType>()
                repeat(size) { list += readResponseWrapped(charset, rawOnly) }
                Push(list)
            }
        }

        RespCode.MAP -> {
            val size = readDecimalCrlf().toInt()
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
private const val VERBATIM_PREFIX_LENGTH = 4 // 3-byte format and ':'

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
        RespCode.INTEGER -> Int64(readDecimalCrlf())
        RespCode.BOOLEAN -> when (readByte()) {
            TRUE_BYTE -> Bool(true)
            FALSE_BYTE -> Bool(false)
            else -> throw ResponseParsingException("Invalid boolean format")
        }.also { skip(2) }

        RespCode.DOUBLE -> F64(readLineStrict().toRespDouble())
        RespCode.BIG_NUMBER -> try {
            BigNumber(BigInteger.parseString(readPartLine(charset)))
        } catch (e: NumberFormatException) {
            throw ResponseParsingException("Invalid BigInteger format", e)
        }

        RespCode.BULK -> {
            val size = readDecimalCrlf().toInt()
            if (size < 0) return RType.Null
            else if (size == 0) {
                skip(2) // skip crlf
                return BulkString(EMPTY_BUFFER)
            }
            val content = Buffer()
            readTo(content, size.toLong())
            skip(2)
            BulkString(content)
        }

        RespCode.BULK_ERROR -> {
            readLineCRLF() // skip size
            RType.Error(readPartLine(charset))
        }

        RespCode.VERBATIM_STRING -> {
            val size = readDecimalCrlf().toInt()
            if (size < 0) return RType.Null
            if (size < VERBATIM_PREFIX_LENGTH) throw ResponseParsingException(
                "Invalid verbatim string length $size, expected at least $VERBATIM_PREFIX_LENGTH",
            )
            val encoding = readByteArray(3).decodeToString()
            readByte() // skip ':' byte
            val content = readSizedText(size - VERBATIM_PREFIX_LENGTH, charset)
            VerbatimString(encoding, content)
        }

        RespCode.NULL -> {
            skip(2) // skip crlf
            RType.Null
        }
        else -> throw ResponseParsingException("Unexpected simple code: $code")
    }
}

private fun Buffer.readPartLine(charset: Charset) = readLineCRLF().readText(charset)

const val CARRIAGE_RETURN_BYTE = '\r'.code.toByte()
const val NEWLINE_BYTE = '\n'.code.toByte()

private inline fun Buffer.readLineCRLF(): Buffer {
    val buffer = Buffer()
    var byte = readByte()
    while (true) {
        if (byte == CARRIAGE_RETURN_BYTE) {
            val nextByte = readByte()
            if (nextByte == NEWLINE_BYTE) break
            // the byte after a lone CR may itself be the CR of the terminator
            buffer.writeByte(CARRIAGE_RETURN_BYTE)
            byte = nextByte
            continue
        }
        buffer.writeByte(byte)
        byte = readByte()
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
        this is RType.Null -> null
        this is PlainString -> if (T::class == String::class) value as T else null
        this is Int64 -> if (T::class == Long::class) value as T else null
        this is Bool -> if (T::class == Boolean::class) value as T else null
        this is F64 -> if (T::class == Double::class) value as T else null
        this is BigNumber -> if (T::class == BigInteger::class) value as T else null
        this is VerbatimString -> if (T::class == String::class) data as T else null
        this is BulkString && T::class == Buffer::class -> value as T
        this is BulkString && T::class == String::class -> value.readString() as T
        this is RArray || this is Push -> throw ResponseParsingException(
            "unwrap() cannot unwrap aggregate ${this::class.simpleName} reply, " +
                "use unwrapList() or unwrapRESPAgnosticMap() for key-value pair arrays",
        )
        this is RSet -> throw ResponseParsingException(
            "unwrap() cannot unwrap aggregate RSet reply, use unwrapSet()",
        )
        this is RMap -> throw ResponseParsingException(
            "unwrap() cannot unwrap aggregate RMap reply, use unwrapMap() or unwrapRESPAgnosticMap()",
        )
        else -> {
            __ParserLogger.warn("Wrong unwrapping [common] method used for $this")
            null
        }
    }
}

/**
 * Unwrap an array or push reply to a list of [T].
 *
 * [RType.Null] elements are skipped; any other element that does not unwrap to [T]
 * fails with [ResponseParsingException] rather than silently shifting the indices.
 */
inline fun <reified T> RType.unwrapList(): List<T> {
    handleEx()
    val response = mutableListOf<T>()
    when (this) {
        is RArray -> {
            value.forEach { i ->
                if (i is RType.Null) return@forEach
                response.add(
                    i.unwrap<T>() ?: throw ResponseParsingException(
                        "unwrapList() cannot unwrap element ${i::class.simpleName} to ${T::class.simpleName}",
                    ),
                )
            }
        }

        is Push -> {
            value.forEach { i ->
                if (i is RType.Null) return@forEach
                response.add(
                    i.unwrap<T>() ?: throw ResponseParsingException(
                        "unwrapList() cannot unwrap element ${i::class.simpleName} to ${T::class.simpleName}",
                    ),
                )
            }
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
    if (this is RArray) {
        if (value.size % 2 != 0) throw ResponseParsingException(
            "unwrapRESPAgnosticMap() expects an even number of array elements, got ${value.size}",
        )
        value.chunked(2).associate {
            it.first().unwrap<L>()!! to it.last().unwrap<R>()
        }
    } else unwrapMap<L, R>()
}

private val logger = KtorSimpleLogger("eu.vendeli.rethis.RTypeParser")

@Suppress("ObjectPropertyName")
val RType.__ParserLogger get() = logger
