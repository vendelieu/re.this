package eu.vendeli.rethis.api.spec.common.decoders.generic

import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.decoders.aggregate.ArrayLongDecoder
import eu.vendeli.rethis.api.spec.common.response.common.WaitAofResult
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.utils.EMPTY_BUFFER
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer


object WaitAofDecoder : ResponseDecoder<WaitAofResult> {
    private val EMPTY_WAIT_AOF_RESULT = WaitAofResult(fsyncedRedises = 0, fsyncedReplicas = 0)
    override suspend fun decode(
        input: Buffer,
        charset: Charset,
        code: RespCode?,
    ): WaitAofResult {
        if (input == EMPTY_BUFFER) return EMPTY_WAIT_AOF_RESULT
        val response = ArrayLongDecoder.decode(input, charset)

        return WaitAofResult(
            fsyncedRedises = response.first(),
            fsyncedReplicas = response.last(),
        )
    }
}
