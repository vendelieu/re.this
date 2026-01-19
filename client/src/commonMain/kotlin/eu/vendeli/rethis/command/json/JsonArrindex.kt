package eu.vendeli.rethis.command.json

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.json.JsonArrIndexCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.jsonArrIndex(
    key: String,
    path: String,
    `value`: String,
    start: Long? = null,
    stop: Long? = null,
): Long {
    val request = if (cfg.withSlots) {
        JsonArrIndexCommandCodec.encodeWithSlot(
            charset = cfg.charset,
            key = key,
            path = path,
            value = value,
            start = start,
            stop = stop,
        )
    } else {
        JsonArrIndexCommandCodec.encode(
            charset = cfg.charset,
            key = key,
            path = path,
            value = value,
            start = start,
            stop = stop,
        )
    }
    return JsonArrIndexCommandCodec.decode(topology.handle(request), cfg.charset)
}
