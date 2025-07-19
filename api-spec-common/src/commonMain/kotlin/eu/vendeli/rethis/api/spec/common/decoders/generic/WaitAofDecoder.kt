package eu.vendeli.rethis.api.spec.common.decoders.generic

import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.decoders.aggregate.ArrayLongDecoder
import eu.vendeli.rethis.api.spec.common.response.common.WaitAofResult
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer


object WaitAofDecoder : ResponseDecoder<WaitAofResult> {
    override suspend fun decode(
        input: Buffer,
        charset: Charset,
        withCode: Boolean,
    ): WaitAofResult {
        val response = ArrayLongDecoder.decode(input, charset)

        return WaitAofResult(
            fsyncedRedises = response.first(),
            fsyncedReplicas = response.last(),
        )
    }
}
