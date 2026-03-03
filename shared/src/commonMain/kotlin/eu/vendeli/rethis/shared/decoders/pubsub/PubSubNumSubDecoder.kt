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
    override fun decode(
        input: Buffer,
        charset: Charset,
        code: RespCode?,
    ): List<PubSubNumEntry> {
        if (input == EMPTY_BUFFER) return emptyList()
        return ArrayRTypeDecoder.decode(input, charset).chunked(2).mapNotNull {
            val channel = it.getOrNull(0)?.unwrap<String>() ?: return@mapNotNull null
            val count = it.getOrNull(1)?.unwrap<Long>() ?: 0L

            PubSubNumEntry(channel, count)
        }
    }
}
