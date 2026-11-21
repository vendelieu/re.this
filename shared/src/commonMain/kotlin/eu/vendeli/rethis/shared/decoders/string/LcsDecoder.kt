package eu.vendeli.rethis.shared.decoders.string

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.response.string.LcsResult
import eu.vendeli.rethis.shared.types.*
import eu.vendeli.rethis.shared.utils.*
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer


object LcsDecoder : ResponseDecoder<LcsResult> {
    private val EMPTY_LCS_RESULT = LcsResult(matches = emptyList(), totalLength = 0L)
    override fun decode(input: Buffer, charset: Charset, code: RespCode?,): LcsResult {
        if (input == EMPTY_BUFFER) return EMPTY_LCS_RESULT
        val response = input.readResponseWrapped(charset)

        val (matches, totalLength) = when (response) {
            is RArray -> Pair(response.value[1].cast<RArray>().value, response.value[3].unwrap<Long>() ?: 0L)

            is RMap -> {
                val mapResponse = response.value.entries.associate { (key, value) ->
                    key.unwrap<String>()!! to value
                }

                Pair(
                    mapResponse["matches"]?.safeCast<RArray>()?.value ?: processingException {
                        "Missing 'matches' field"
                    },
                    mapResponse["len"]?.unwrap<Long>() ?: 0L,
                )
            }

            else -> processingException { "Wrong response type" }
        }

        return LcsResult(processMatches(matches), totalLength)
    }

    private fun processMatches(matchesArr: List<Any>): List<LcsResult.LcsMatch> = matchesArr.map { matchGroup ->
        val parts = matchGroup.safeCast<RArray>()?.value ?: processingException { "Unexpected match type" }
        if (parts.size < 2) processingException { "Unexpected match size ${parts.size}" }

        LcsResult.LcsMatch(
            first = parts[0].toRange(),
            second = parts[1].toRange(),
            length = parts.getOrNull(2)?.safeCast<Int64>()?.value,
        )
    }

    private fun Any.toRange(): LongRange {
        val bounds = safeCast<RArray>()?.value ?: processingException { "Unexpected match range type" }
        if (bounds.size != 2) processingException { "Unexpected match range size ${bounds.size}" }

        return (bounds[0].unwrap<Long>() ?: 0L)..(bounds[1].unwrap<Long>() ?: 0L)
    }
}
