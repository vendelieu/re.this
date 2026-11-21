package eu.vendeli.rethis.shared.decoders.common

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.decoders.aggregate.ArrayRTypeDecoder
import eu.vendeli.rethis.shared.response.common.ScanResult
import eu.vendeli.rethis.shared.types.RArray
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.ResponseParsingException
import eu.vendeli.rethis.shared.types.processingException
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.safeCast
import eu.vendeli.rethis.shared.utils.unwrap
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer


object PairScanDecoder : ResponseDecoder<ScanResult<Pair<String, String>>> {
    private val EMPTY_SCAN_RESULT = ScanResult<Pair<String, String>>(cursor = "", keys = emptyList())
    override fun decode(
        input: Buffer,
        charset: Charset,
        code: RespCode?,
    ): ScanResult<Pair<String, String>> {
        if (input == EMPTY_BUFFER) return EMPTY_SCAN_RESULT
        val arrResponse = ArrayRTypeDecoder.decode(input, charset, code)
        if (arrResponse.size != 2) throw ResponseParsingException(
            "Invalid response structure, expected cursor and keys, given ${arrResponse.size} elements",
        )

        val newCursor = arrResponse[0].unwrap<String>() ?: processingException { "Missing cursor in response" }

        val keysArray = arrResponse[1].safeCast<RArray>()?.value ?: processingException { "Missing keys in response" }
        if (keysArray.size % 2 != 0) throw ResponseParsingException(
            "Invalid response structure, expected field-value pairs, given ${keysArray.size} elements",
        )
        val keys = keysArray.chunked(2) {
            val field = it.first().unwrap<String>() ?: throw ResponseParsingException("Invalid field format")
            val value = it.last().unwrap<String>() ?: throw ResponseParsingException("Invalid value format")
            field to value
        }

        return ScanResult(cursor = newCursor, keys = keys)
    }
}
