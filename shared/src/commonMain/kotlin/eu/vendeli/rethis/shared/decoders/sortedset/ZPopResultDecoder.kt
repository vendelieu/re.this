package eu.vendeli.rethis.shared.decoders.sortedset

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.decoders.aggregate.ArrayRTypeDecoder
import eu.vendeli.rethis.shared.response.stream.ZPopResult
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.unwrap
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer


object ZPopResultDecoder : ResponseDecoder<ZPopResult> {
    private val EMPTY_POP_RESULT = ZPopResult(key = "", popped = "", score = 0.0)
    override suspend fun decode(
        input: Buffer,
        charset: Charset,
        code: RespCode?,
    ): ZPopResult {
        if (input == EMPTY_BUFFER) return EMPTY_POP_RESULT
        return ArrayRTypeDecoder.decode(input, charset).let {
            ZPopResult(key = it[0].unwrap()!!, popped = it[1].unwrap()!!, score = it[2].unwrap<String>()!!.toDouble())
        }
    }
}

