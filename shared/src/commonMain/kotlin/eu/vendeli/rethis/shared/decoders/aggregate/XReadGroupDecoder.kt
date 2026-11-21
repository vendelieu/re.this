package eu.vendeli.rethis.shared.decoders.aggregate

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.types.*
import eu.vendeli.rethis.shared.types.stream.XReadGroupMessage
import eu.vendeli.rethis.shared.types.stream.XReadGroupResponse
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.readDecimalCrlf
import eu.vendeli.rethis.shared.utils.readResponseWrapped
import eu.vendeli.rethis.shared.utils.safeCast
import eu.vendeli.rethis.shared.utils.tryInferCause
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.readString

object XReadGroupDecoder : ResponseDecoder<List<XReadGroupResponse>> {
    override fun decode(
        input: Buffer,
        charset: Charset,
        code: RespCode?,
    ): List<XReadGroupResponse> {
        if (input == EMPTY_BUFFER) return emptyList()
        val actualCode = code ?: RespCode.fromCode(input.readByte())
        if (actualCode == RespCode.NULL) {
            input.skip(2) // trailing CRLF of the null marker
            return emptyList()
        }
        if (actualCode != RespCode.ARRAY && actualCode != RespCode.MAP) {
            throw input.tryInferCause(actualCode, charset) ?: ResponseParsingException(
                "Invalid response structure, expected ARRAY or MAP token, given $actualCode",
            )
        }
        val size = input.readDecimalCrlf().toInt()
        if (size == 0) return emptyList()

        return buildList {
            repeat(size) {
                var streamName: String? = null
                var messagesArray: List<RType>? = null
                when (actualCode) {
                    RespCode.ARRAY -> {
                        val streamArray = input.readResponseWrapped(charset).safeCast<RArray>()?.value
                            ?: throw malformed("stream entry is not an array")
                        if (streamArray.size < 2) throw malformed("stream entry has ${streamArray.size} elements")
                        streamName = when (val sNameType = streamArray[0]) {
                            is BulkString -> sNameType.value.readString()
                            else -> sNameType.value.toString()
                        }
                        messagesArray = streamArray[1].safeCast<RArray>()?.value
                            ?: throw malformed("stream messages are not an array")
                    }
                    RespCode.MAP -> {
                        val keyType = input.readResponseWrapped(charset)
                        val valueType = input.readResponseWrapped(charset)
                        streamName = when (keyType) {
                            is BulkString -> keyType.value.readString()
                            else -> keyType.toString()
                        }
                        messagesArray = (valueType as? RArray)?.value
                            ?: throw malformed("stream messages are not an array")
                    }
                    else -> throw malformed("unexpected token $actualCode")
                }
                val name = streamName
                val arr = messagesArray
                val messages = arr.map { msg ->
                    val msgArray = (msg as? RArray)?.value ?: throw malformed("message entry is not an array")
                    if (msgArray.size < 2) throw malformed("message entry has ${msgArray.size} elements")

                    val id = when (val idType = msgArray[0]) {
                        is BulkString -> idType.value.readString()
                        else -> idType.value.toString()
                    }

                    val data = when (val dataRaw = msgArray[1]) {
                        is RMap -> dataRaw.value.entries.associate {
                            val k = when (val kType = it.key) {
                                is BulkString -> kType.value.readString()
                                else -> kType.value.toString()
                            }
                            k to (it.value ?: RType.Null)
                        }

                        is RArray -> {
                            val map = mutableMapOf<String, RType>()
                            for (i in 0 until dataRaw.value.size step 2) {
                                val k = when (val kType = dataRaw.value[i]) {
                                    is BulkString -> kType.value.readString()
                                    else -> kType.value.toString()
                                }
                                val v = dataRaw.value.getOrNull(i + 1) ?: RType.Null
                                map[k] = v
                            }
                            map
                        }

                        else -> emptyMap()
                    }
                    XReadGroupMessage(id, data)
                }
                add(XReadGroupResponse(name, messages))
            }
        }
    }

    fun decodeNullable(
        input: Buffer,
        charset: Charset,
        code: RespCode?
    ): List<XReadGroupResponse>? {
        if (input == EMPTY_BUFFER) return emptyList()
        val actualCode = code ?: RespCode.fromCode(input.readByte())
        if (actualCode == RespCode.NULL) {
            input.skip(2) // trailing CRLF of the null marker
            return null
        }
        if (actualCode == RespCode.ARRAY && input.startsWithNullSize()) {
            input.skip(NULL_SIZE_LINE.size.toLong()) // RESP2 null array, the reply of a timed out BLOCK
            return null
        }

        return decode(input, charset, actualCode)
    }

    private val NULL_SIZE_LINE = "-1\r\n".encodeToByteArray()

    private fun Buffer.startsWithNullSize(): Boolean =
        size >= NULL_SIZE_LINE.size && NULL_SIZE_LINE.indices.all { this[it.toLong()] == NULL_SIZE_LINE[it] }

    private fun malformed(reason: String) = ResponseParsingException("Invalid XREADGROUP response structure, $reason")
}
