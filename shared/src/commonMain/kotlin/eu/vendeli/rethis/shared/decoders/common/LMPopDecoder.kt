package eu.vendeli.rethis.shared.decoders.common

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.response.common.MPopResult
import eu.vendeli.rethis.shared.types.RArray
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.cast
import eu.vendeli.rethis.shared.utils.readResponseWrapped
import eu.vendeli.rethis.shared.utils.unwrap
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer

object LMPopDecoder : ResponseDecoder<List<MPopResult>> {
    override fun decode(
        input: Buffer,
        charset: Charset,
        code: RespCode?,
    ): List<MPopResult> {
        if (input == EMPTY_BUFFER) return emptyList()
        val response = input.readResponseWrapped(charset)

        val elements = response.cast<RArray>().value
        return elements.chunked(2) { item ->
            MPopResult(
                name = item.first().unwrap<String>()!!,
                poppedElements = when (val popped = item.last()) {
                    is RArray -> popped.value.flatMap { element ->
                        // ZMPOP/BZMPOP wrap every popped member with its score in a nested array
                        if (element is RArray) element.value.map { it.stringified() } else listOf(element.stringified())
                    }

                    // flat member-score replies (e.g. ZPOPMAX)
                    else -> listOf(popped.stringified())
                },
            )
        }
    }

    private fun RType.stringified(): String = unwrap<String>() ?: requireNotNull(value).toString()
}
