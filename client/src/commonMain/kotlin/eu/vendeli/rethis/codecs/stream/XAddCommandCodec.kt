package eu.vendeli.rethis.codecs.stream

import eu.vendeli.rethis.shared.decoders.general.BulkStringDecoder
import eu.vendeli.rethis.shared.request.common.FieldValue
import eu.vendeli.rethis.shared.request.stream.*
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.UnexpectedResponseType
import eu.vendeli.rethis.shared.utils.CRC16
import eu.vendeli.rethis.shared.utils.tryInferCause
import eu.vendeli.rethis.shared.utils.validateSlot
import eu.vendeli.rethis.utils.parseCode
import eu.vendeli.rethis.utils.writeLongArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object XAddCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$4\r\nXADD\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        key: String,
        nomkstream: Boolean?,
        trim: XAddOption.Trim?,
        idSelector: XAddOption.Identifier,
        vararg `data`: FieldValue,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(key, charset, )
        nomkstream?.let { it0 ->
            if(it0) {
                size += 1
                buffer.writeStringArg("NOMKSTREAM", charset)
            }
        }
        trim?.let { it1 ->
            when (it1.strategy) {
                is MAXLEN ->  {
                    size += 1
                    buffer.writeStringArg(it1.toString(), charset)
                }
                is MINID ->  {
                    size += 1
                    buffer.writeStringArg(it1.toString(), charset)
                }
            }
            it1.operator?.let { it2 ->
                when (it2) {
                    is Approximate ->  {
                        size += 1
                        buffer.writeStringArg("~", charset)
                    }
                    is Equal ->  {
                        size += 1
                        buffer.writeStringArg("=", charset)
                    }
                }
            }
            size += 1
            buffer.writeStringArg(it1.threshold, charset, )
            it1.count?.let { it3 ->
                size += 1
                buffer.writeStringArg("LIMIT", charset)
                size += 1
                buffer.writeLongArg(it3, charset, )
            }
        }
        when (idSelector) {
            is XAddOption.Asterisk ->  {
                size += 1
                buffer.writeStringArg("*", charset)
            }
            is XAddOption.Id ->  {
                size += 1
                buffer.writeStringArg(idSelector.id, charset, )
            }
        }
        data.forEach { it4 ->
            size += 1
            buffer.writeStringArg(it4.field, charset, )
            size += 1
            buffer.writeStringArg(it4.value, charset, )
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.WRITE, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        key: String,
        nomkstream: Boolean?,
        trim: XAddOption.Trim?,
        idSelector: XAddOption.Identifier,
        vararg `data`: FieldValue,
    ): CommandRequest {
        var slot: Int? = null
        slot = validateSlot(slot, CRC16.lookup(key.toByteArray(charset)))
        val request = encode(charset, key = key, nomkstream = nomkstream, trim = trim, idSelector = idSelector, data = data)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): String? {
        val code = input.parseCode(RespCode.BULK)
        return when(code) {
            RespCode.BULK -> {
                BulkStringDecoder.decode(input, charset, code)
            }
            RespCode.NULL -> {
                null
            }
            else -> {
                throw UnexpectedResponseType("Expected [BULK, NULL] but got $code", input.tryInferCause(code))
            }
        }
    }
}
