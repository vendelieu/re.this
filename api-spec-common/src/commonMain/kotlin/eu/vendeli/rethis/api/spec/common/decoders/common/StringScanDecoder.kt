package eu.vendeli.rethis.api.spec.common.decoders.common

import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.decoders.aggregate.ArrayRTypeDecoder
import eu.vendeli.rethis.api.spec.common.response.ScanResult
import eu.vendeli.rethis.api.spec.common.types.RArray
import eu.vendeli.rethis.api.spec.common.types.processingException
import eu.vendeli.rethis.api.spec.common.utils.safeCast
import eu.vendeli.rethis.api.spec.common.utils.unwrap
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer


object StringScanDecoder : ResponseDecoder<ScanResult<String>> {
    override suspend fun decode(
        input: Buffer,
        charset: Charset,
        withCode: Boolean,
    ): ScanResult<String> {
        val arrResponse = ArrayRTypeDecoder.decode(input, charset)

        val newCursor = arrResponse[0].unwrap<String>() ?: processingException { "Missing cursor in response" }

        val keysArray = arrResponse[1].safeCast<RArray>()?.value ?: processingException { "Missing keys in response" }
        val keys = keysArray.map { it.unwrap<String>() ?: processingException { "Invalid key format" } }

        return ScanResult(cursor = newCursor, keys = keys)
    }
}
