package eu.vendeli.rethis.shared.decoders.common

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.decoders.aggregate.ArrayRTypeDecoder
import eu.vendeli.rethis.shared.response.common.ScanResult
import eu.vendeli.rethis.shared.types.RArray
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.processingException
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.safeCast
import eu.vendeli.rethis.shared.utils.unwrap
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer


object StringScanDecoder : ResponseDecoder<ScanResult<String>> {
    private val EMPTY_SCAN_RESULT = ScanResult<String>(cursor = "", keys = emptyList())
    override suspend fun decode(
        input: Buffer,
        charset: Charset,
        code: RespCode?,
    ): ScanResult<String> {
        if (input == EMPTY_BUFFER) return EMPTY_SCAN_RESULT
        val arrResponse = ArrayRTypeDecoder.decode(input, charset)

        val newCursor = arrResponse[0].unwrap<String>() ?: processingException { "Missing cursor in response" }

        val keysArray = arrResponse[1].safeCast<RArray>()?.value ?: processingException { "Missing keys in response" }
        val keys = keysArray.map { it.unwrap<String>() ?: processingException { "Invalid key format" } }

        return ScanResult(cursor = newCursor, keys = keys)
    }
}
