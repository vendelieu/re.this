package eu.vendeli.rethis.command.string

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.string.AppendCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.append(key: String, `value`: String): Long {
    val request = if (cfg.withSlots) {
        AppendCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, value = value)
    } else {
        AppendCommandCodec.encode(charset = cfg.charset, key = key, value = value)
    }
    return AppendCommandCodec.decode(topology.handle(request), cfg.charset)
}
