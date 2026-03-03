package eu.vendeli.rethis.shared.decoders.aggregate

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.types.*
import eu.vendeli.rethis.shared.types.stream.XReadGroupMessage
import eu.vendeli.rethis.shared.types.stream.XReadGroupResponse
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.readResponseWrapped
import eu.vendeli.rethis.shared.utils.safeCast
import eu.vendeli.rethis.shared.utils.tryInferCause
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.readLineStrict
import kotlinx.io.readString

object XReadGroupDecoder : ResponseDecoder<List<XReadGroupResponse>> {
    override fun decode(
        input: Buffer,
        charset: Charset,
        code: RespCode?,
    ): List<XReadGroupResponse> {
        if (input == EMPTY_BUFFER) return emptyList()
        val actualCode = code ?: RespCode.fromCode(input.readByte())
        if (actualCode == RespCode.NULL) return emptyList()
        if (actualCode != RespCode.ARRAY && actualCode != RespCode.MAP) {
            throw ResponseParsingException(
                "Invalid response structure, expected ARRAY or MAP token, given $actualCode",
                input.tryInferCause(actualCode),
            )
        }
        val size = try {
            input.readLineStrict().toInt()
        } catch (_: Exception) {
            0
        }
        if (size == 0) return emptyList()

        return buildList {
            repeat(size) {
                var streamName: String? = null
                var messagesArray: List<RType>? = null
                when (actualCode) {
                    RespCode.ARRAY -> {
                        val streamArray = input.readResponseWrapped(charset).safeCast<RArray>()?.value ?: return@repeat
                        if (streamArray.size < 2) return@repeat
                        streamName = when (val sNameType = streamArray[0]) {
                            is BulkString -> sNameType.value.readString()
                            else -> sNameType.value.toString()
                        }
                        messagesArray = streamArray[1].safeCast<RArray>()?.value ?: return@repeat
                    }
                    RespCode.MAP -> {
                        val keyType = input.readResponseWrapped(charset)
                        val valueType = input.readResponseWrapped(charset)
                        streamName = when (keyType) {
                            is BulkString -> keyType.value.readString()
                            else -> keyType.toString()
                        }
                        messagesArray = (valueType as? RArray)?.value ?: return@repeat
                    }
                    else -> return@repeat
                }
                val name = streamName
                val arr = messagesArray
                val messages = arr.mapNotNull { msg ->
                    val msgArray = (msg as? RArray)?.value ?: return@mapNotNull null
                    if (msgArray.size < 2) return@mapNotNull null

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
    ): List<XReadGroupResponse>? =
        decode(
            input,
            charset,
            code
        ).ifEmpty { if (code == RespCode.NULL) null else emptyList() }
}
