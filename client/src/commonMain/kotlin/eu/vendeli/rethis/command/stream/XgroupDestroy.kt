package eu.vendeli.rethis.command.stream

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.stream.XGroupDestroyCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String

public suspend fun ReThis.xGroupDestroy(key: String, group: String): Long {
    val request = if(cfg.withSlots) {
        XGroupDestroyCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, group = group)
    } else {
        XGroupDestroyCommandCodec.encode(charset = cfg.charset, key = key, group = group)
    }
    return XGroupDestroyCommandCodec.decode(topology.handle(request), cfg.charset)
}
