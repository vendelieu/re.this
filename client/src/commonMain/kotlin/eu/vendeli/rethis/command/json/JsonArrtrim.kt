package eu.vendeli.rethis.command.json

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.json.JsonArrTrimCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.jsonArrTrim(
    key: String,
    path: String,
    start: Long,
    stop: Long,
): Long {
    val request = if(cfg.withSlots) {
        JsonArrTrimCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, path = path, start = start, stop = stop)
    } else {
        JsonArrTrimCommandCodec.encode(charset = cfg.charset, key = key, path = path, start = start, stop = stop)
    }
    return JsonArrTrimCommandCodec.decode(topology.handle(request), cfg.charset)
}
