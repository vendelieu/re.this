package eu.vendeli.rethis.command.stream

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.stream.XAckCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.xAck(
    key: String,
    group: String,
    vararg id: String,
): Long {
    val request = if(cfg.withSlots) {
        XAckCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, group = group, id = id)
    } else {
        XAckCommandCodec.encode(charset = cfg.charset, key = key, group = group, id = id)
    }
    return XAckCommandCodec.decode(topology.handle(request), cfg.charset)
}
