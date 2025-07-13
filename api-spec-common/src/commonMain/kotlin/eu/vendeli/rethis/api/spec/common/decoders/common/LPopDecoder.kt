package eu.vendeli.rethis.api.spec.common.decoders.common

import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.response.PopResult
import eu.vendeli.rethis.api.spec.common.types.RArray
import eu.vendeli.rethis.api.spec.common.utils.readResponseWrapped
import eu.vendeli.rethis.api.spec.common.utils.unwrap
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer

object LPopDecoder : ResponseDecoder<PopResult> {
    override suspend fun decode(
        input: Buffer,
        charset: Charset,
        withCode: Boolean,
    ): PopResult {
        val response = input.readResponseWrapped(charset)

        val elements = (response as RArray).value
        return PopResult(key = elements.first().unwrap()!!, popped = elements.last().unwrap()!!)
    }
}
