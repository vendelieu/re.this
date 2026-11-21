package eu.vendeli.rethis.shared.decoders.sentinel

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.decoders.general.SimpleStringDecoder
import eu.vendeli.rethis.shared.types.RespCode
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer

/**
 * `SENTINEL CKQUORUM` replies with `OK <n> usable Sentinels. ...` when the quorum can be reached
 * (failures arrive as errors), so any message starting with `OK` means success.
 */
object SentinelCkQuorumDecoder : ResponseDecoder<Boolean> {
    override fun decode(input: Buffer, charset: Charset, code: RespCode?): Boolean =
        SimpleStringDecoder.decode(input, charset, code).startsWith("OK")
}
