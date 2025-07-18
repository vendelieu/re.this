package eu.vendeli.rethis.command.stream

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.stream.XGroupCreateConsumerCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String

public suspend fun ReThis.xGroupCreateConsumer(
    key: String,
    group: String,
    consumer: String,
): Long {
    val request = if(cfg.withSlots) {
        XGroupCreateConsumerCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, group = group, consumer = consumer)
    } else {
        XGroupCreateConsumerCommandCodec.encode(charset = cfg.charset, key = key, group = group, consumer = consumer)
    }
    return XGroupCreateConsumerCommandCodec.decode(topology.handle(request), cfg.charset)
}
