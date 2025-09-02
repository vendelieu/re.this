package eu.vendeli.rethis.shared.decoders.common

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.response.common.PopResult
import eu.vendeli.rethis.shared.types.RArray
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.readResponseWrapped
import eu.vendeli.rethis.shared.utils.unwrap
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer

object LPopDecoder : ResponseDecoder<PopResult> {
    private val EMPTY_POP_RESULT = PopResult(key = "", popped = "")
    override suspend fun decode(
        input: Buffer,
        charset: Charset,
        code: RespCode?,
    ): PopResult {
        if (input == EMPTY_BUFFER) return EMPTY_POP_RESULT
        val response = input.readResponseWrapped(charset)

        val elements = (response as RArray).value
        return PopResult(key = elements.first().unwrap()!!, popped = elements.last().unwrap()!!)
    }
}
