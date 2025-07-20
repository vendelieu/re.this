package eu.vendeli.rethis.api.spec.common.decoders.pubsub

import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.decoders.aggregate.ArrayRTypeDecoder
import eu.vendeli.rethis.api.spec.common.response.common.PubSubNumEntry
import eu.vendeli.rethis.api.spec.common.utils.EMPTY_BUFFER
import eu.vendeli.rethis.api.spec.common.utils.unwrap
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer


object PubSubNumSubDecoder : ResponseDecoder<List<PubSubNumEntry>> {
    override suspend fun decode(
        input: Buffer,
        charset: Charset,
        withCode: Boolean,
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
