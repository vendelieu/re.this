package eu.vendeli.rethis.shared.decoders.common

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.response.common.PopResult
import eu.vendeli.rethis.shared.types.RArray
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.ResponseParsingException
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.readResponseWrapped
import eu.vendeli.rethis.shared.utils.unwrap
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer

/**
 * Decodes the `[key, element]` reply of BLPOP/BRPOP. A blocking timeout (`*-1` on RESP2, `_` on RESP3)
 * and a queued placeholder decode to `null`.
 */
object LPopDecoder : ResponseDecoder<PopResult?> {
    override fun decode(
        input: Buffer,
        charset: Charset,
        code: RespCode?,
    ): PopResult? {
        if (input == EMPTY_BUFFER) return null
        val response = input.readResponseWrapped(charset, code = code)
        if (response is RType.Null) return null

        val elements = (response as? RArray)?.value
            ?: throw ResponseParsingException("Invalid response structure, expected array, given $response")
        if (elements.size != 2) throw ResponseParsingException(
            "Invalid response structure, expected key and popped element, given ${elements.size} elements",
        )

        return PopResult(
            key = elements.first().unwrap<String>()
                ?: throw ResponseParsingException("Invalid response structure, missing key"),
            popped = elements.last().unwrap<String>()
                ?: throw ResponseParsingException("Invalid response structure, missing popped element"),
        )
    }
}
