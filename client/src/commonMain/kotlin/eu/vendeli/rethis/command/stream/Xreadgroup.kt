package eu.vendeli.rethis.command.stream

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.stream.XReadGroupCommandCodec
import eu.vendeli.rethis.shared.request.stream.XReadGroupKeyIds
import eu.vendeli.rethis.shared.request.stream.XReadGroupOption
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.xReadGroup(
    group: String,
    consumer: String,
    streams: XReadGroupKeyIds,
    vararg option: XReadGroupOption,
): Map<String, RType>? {
    val request = if (cfg.withSlots) {
        XReadGroupCommandCodec.encodeWithSlot(
            charset = cfg.charset,
            group = group,
            consumer = consumer,
            streams = streams,
            option = option,
        )
    } else {
        XReadGroupCommandCodec.encode(
            charset = cfg.charset,
            group = group,
            consumer = consumer,
            streams = streams,
            option = option,
        )
    }
    return XReadGroupCommandCodec.decode(topology.handle(request), cfg.charset)
}
