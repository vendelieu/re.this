package eu.vendeli.rethis.shared.decoders.sentinel

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.decoders.general.IntegerDecoder
import eu.vendeli.rethis.shared.types.RespCode
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer

/**
 * `SENTINEL RESET` replies with the number of masters that were reset; `true` when at least one was.
 */
object SentinelResetDecoder : ResponseDecoder<Boolean> {
    override fun decode(input: Buffer, charset: Charset, code: RespCode?): Boolean =
        IntegerDecoder.decode(input, charset, code) > 0L
}
