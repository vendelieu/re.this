package eu.vendeli.rethis.shared.decoders.sentinel

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.decoders.aggregate.ArrayStringDecoder
import eu.vendeli.rethis.shared.types.ResponseParsingException
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.readDecimalCrlf
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer

/**
 * `SENTINEL GET-MASTER-ADDR-BY-NAME` replies with `[host, port]`, or with a null array
 * (`*-1` on RESP2, `_` on RESP3) when the sentinel does not know the master.
 */
object SentinelGetMasterAddrDecoder : ResponseDecoder<List<String>?> {
    override fun decode(input: Buffer, charset: Charset, code: RespCode?): List<String>? {
        if (input == EMPTY_BUFFER) return emptyList()
        val type = code ?: RespCode.fromCode(input.readByte())
        return when (type) {
            RespCode.NULL -> null

            RespCode.ARRAY -> {
                val size = input.copy().readDecimalCrlf()
                if (size < 0) {
                    input.readDecimalCrlf()
                    null
                } else {
                    ArrayStringDecoder.decode(input, charset, type)
                }
            }

            else -> throw ResponseParsingException("Invalid response structure, expected ARRAY or NULL, given $type")
        }
    }
}
