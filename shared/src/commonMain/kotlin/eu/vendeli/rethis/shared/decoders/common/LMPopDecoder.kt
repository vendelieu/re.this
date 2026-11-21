package eu.vendeli.rethis.shared.decoders.common

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.response.common.MPopResult
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
 * Decodes the `[key, elements]` reply of LMPOP/BLMPOP/ZMPOP/BZMPOP. A reply without elements (`*-1` on RESP2, `_` on
 * RESP3, for example a blocking timeout) and a queued placeholder decode to `null`.
 */
object LMPopDecoder : ResponseDecoder<List<MPopResult>?> {
    override fun decode(
        input: Buffer,
        charset: Charset,
        code: RespCode?,
    ): List<MPopResult>? {
        if (input == EMPTY_BUFFER) return null
        val response = input.readResponseWrapped(charset, code = code)
        if (response is RType.Null) return null

        val elements = (response as? RArray)?.value
            ?: throw ResponseParsingException("Invalid response structure, expected array, given $response")
        return elements.chunked(2) { item ->
            if (item.size != 2) throw ResponseParsingException(
                "Invalid response structure, expected name and popped elements pair",
            )

            MPopResult(
                name = item.first().unwrap<String>()
                    ?: throw ResponseParsingException("Invalid response structure, missing name"),
                poppedElements = when (val popped = item.last()) {
                    is RArray -> popped.value.flatMap { element ->
                        // ZMPOP/BZMPOP wrap every popped member with its score in a nested array
                        if (element is RArray) element.value.map { it.stringified() } else listOf(element.stringified())
                    }

                    // flat member-score replies
                    else -> listOf(popped.stringified())
                },
            )
        }
    }

    private fun RType.stringified(): String = unwrap<String>() ?: requireNotNull(value).toString()
}
