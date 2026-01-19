package eu.vendeli.rethis.command.stream

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.stream.XInfoConsumersCommandCodec
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.xInfoConsumers(key: String, group: String): List<RType> {
    val request = if (cfg.withSlots) {
        XInfoConsumersCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, group = group)
    } else {
        XInfoConsumersCommandCodec.encode(charset = cfg.charset, key = key, group = group)
    }
    return XInfoConsumersCommandCodec.decode(topology.handle(request), cfg.charset)
}
