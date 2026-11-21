package eu.vendeli.rethis.shared.decoders.sortedset

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.response.sortedset.ZPopResult
import eu.vendeli.rethis.shared.types.RArray
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.ResponseParsingException
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.readResponseWrapped
import eu.vendeli.rethis.shared.utils.toRespDouble
import eu.vendeli.rethis.shared.utils.unwrap
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer

private const val ZPOP_RESULT_SIZE = 3

/**
 * Decodes the `[key, member, score]` reply of BZPOPMIN/BZPOPMAX. A blocking timeout
 * (`*-1` on RESP2, `_` on RESP3) and a queued placeholder decode to `null`.
 */
object ZPopResultDecoder : ResponseDecoder<ZPopResult?> {
    override fun decode(
        input: Buffer,
        charset: Charset,
        code: RespCode?,
    ): ZPopResult? {
        if (input == EMPTY_BUFFER) return null
        val response = input.readResponseWrapped(charset, code = code)
        if (response is RType.Null) return null

        val elements = (response as? RArray)?.value
            ?: throw ResponseParsingException("Invalid response structure, expected array, given $response")
        if (elements.isEmpty()) return null
        if (elements.size != ZPOP_RESULT_SIZE) throw ResponseParsingException(
            "Invalid response structure, expected key, member and score, given ${elements.size} elements",
        )

        return ZPopResult(
            key = elements[0].unwrap<String>()
                ?: throw ResponseParsingException("Invalid response structure, missing key"),
            popped = elements[1].unwrap<String>()
                ?: throw ResponseParsingException("Invalid response structure, missing member"),
            score = elements[2].unwrap<String>()?.toRespDouble()
                ?: elements[2].unwrap<Double>()
                ?: throw ResponseParsingException("Invalid response structure, missing score"),
        )
    }
}
