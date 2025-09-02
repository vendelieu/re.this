package eu.vendeli.rethis.shared.decoders.pubsub

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.decoders.aggregate.ArrayRTypeDecoder
import eu.vendeli.rethis.shared.response.common.PubSubNumEntry
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.unwrap
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer


object PubSubNumSubDecoder : ResponseDecoder<List<PubSubNumEntry>> {
    override suspend fun decode(
        input: Buffer,
        charset: Charset,
        code: RespCode?,
    ): List<PubSubNumEntry> {
        if (input == EMPTY_BUFFER) return emptyList()
        return ArrayRTypeDecoder.decode(input, charset).chunked(2) {
            PubSubNumEntry(
                it.first().unwrap<String>()!!,
                it.last().unwrap() ?: 0,
            )
        }
    }
}
