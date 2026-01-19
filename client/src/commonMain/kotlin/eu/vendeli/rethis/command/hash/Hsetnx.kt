package eu.vendeli.rethis.command.hash

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.hash.HSetNxCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.hSetNx(
    key: String,
    `field`: String,
    `value`: String,
): Long {
    val request = if (cfg.withSlots) {
        HSetNxCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, field = field, value = value)
    } else {
        HSetNxCommandCodec.encode(charset = cfg.charset, key = key, field = field, value = value)
    }
    return HSetNxCommandCodec.decode(topology.handle(request), cfg.charset)
}
