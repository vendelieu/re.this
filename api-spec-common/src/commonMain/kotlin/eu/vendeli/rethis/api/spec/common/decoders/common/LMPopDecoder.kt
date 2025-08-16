package eu.vendeli.rethis.api.spec.common.decoders.common

import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.response.common.MPopResult
import eu.vendeli.rethis.api.spec.common.types.RArray
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.utils.EMPTY_BUFFER
import eu.vendeli.rethis.api.spec.common.utils.cast
import eu.vendeli.rethis.api.spec.common.utils.readResponseWrapped
import eu.vendeli.rethis.api.spec.common.utils.unwrap
import eu.vendeli.rethis.api.spec.common.utils.unwrapList
import eu.vendeli.rethis.api.spec.common.utils.unwrapSet
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer

object LMPopDecoder : ResponseDecoder<List<MPopResult>> {
    override suspend fun decode(
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
                poppedElements = item.last().unwrapList<String>().toList(),
            )
        }
    }
}
