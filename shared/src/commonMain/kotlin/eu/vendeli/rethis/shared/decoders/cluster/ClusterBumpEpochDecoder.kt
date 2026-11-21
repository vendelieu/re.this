package eu.vendeli.rethis.shared.decoders.cluster

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.decoders.general.BulkStringDecoder
import eu.vendeli.rethis.shared.decoders.general.SimpleStringDecoder
import eu.vendeli.rethis.shared.decoders.general.VerbatimStringDecoder
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.UnexpectedResponseType
import eu.vendeli.rethis.shared.utils.tryInferCause
import eu.vendeli.rethis.utils.parseCode
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer

/**
 * `CLUSTER BUMPEPOCH` replies with the simple string `BUMPED <epoch>` when the config epoch was
 * advanced and `STILL <epoch>` when the node already had the greatest one; `true` only for `BUMPED`.
 * Bulk and verbatim strings are accepted as well.
 */
object ClusterBumpEpochDecoder : ResponseDecoder<Boolean> {
    override fun decode(input: Buffer, charset: Charset, code: RespCode?): Boolean {
        val type = code ?: input.parseCode(RespCode.SIMPLE_STRING)
        val text = when (type) {
            RespCode.SIMPLE_STRING -> SimpleStringDecoder.decode(input, charset, type)
            RespCode.BULK -> BulkStringDecoder.decode(input, charset, type)
            RespCode.VERBATIM_STRING -> VerbatimStringDecoder.decode(input, charset, type)
            else -> throw input.tryInferCause(type, charset) ?: UnexpectedResponseType(
                "Expected [SIMPLE_STRING, BULK, VERBATIM_STRING] but got $type",
            )
        }

        return text.startsWith("BUMPED")
    }
}
