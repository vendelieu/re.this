package eu.vendeli.rethis.api.spec.common.decoders.string

import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.response.LcsResult
import eu.vendeli.rethis.api.spec.common.types.Int64
import eu.vendeli.rethis.api.spec.common.types.RArray
import eu.vendeli.rethis.api.spec.common.types.RMap
import eu.vendeli.rethis.api.spec.common.types.processingException
import eu.vendeli.rethis.api.spec.common.utils.cast
import eu.vendeli.rethis.api.spec.common.utils.readResponseWrapped
import eu.vendeli.rethis.api.spec.common.utils.safeCast
import eu.vendeli.rethis.api.spec.common.utils.unwrap
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer


object LcsDecoder : ResponseDecoder<LcsResult> {
    override suspend fun decode(input: Buffer, charset: Charset, withCode: Boolean): LcsResult {
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

    private fun processMatches(matchesArr: List<Any>): List<List<LcsResult.LcsMatch>> =
        matchesArr.map { matchGroup ->
            val length = matchGroup.safeCast<RArray>()?.value?.getOrNull(2)?.let { match ->
                match.safeCast<Int64>()?.value
            }
            matchGroup.cast<RArray>().value.dropLast(1).map { match ->
                if (match is RArray) {
                    val range = match.value
                    val start = range[0].unwrap<Long>() ?: 0L
                    val end = range[1].unwrap<Long>() ?: 0L
                    LcsResult.LcsMatch(start = start, end = end, length = length)
                } else {
                    processingException { "Unexpected match type" }
                }
            }
        }
}
