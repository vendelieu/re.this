package eu.vendeli.rethis.command.stream

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.stream.XGroupDelConsumerCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.xGroupDelConsumer(
    key: String,
    group: String,
    consumer: String,
): Long {
    val request = if (cfg.withSlots) {
        XGroupDelConsumerCommandCodec.encodeWithSlot(
            charset = cfg.charset,
            key = key,
            group = group,
            consumer = consumer,
        )
    } else {
        XGroupDelConsumerCommandCodec.encode(charset = cfg.charset, key = key, group = group, consumer = consumer)
    }
    return XGroupDelConsumerCommandCodec.decode(topology.handle(request), cfg.charset)
}
