package eu.vendeli.rethis.command.json

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.json.JsonArrLenCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.jsonArrLen(key: String, path: String? = null): Long {
    val request = if (cfg.withSlots) {
        JsonArrLenCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, path = path)
    } else {
        JsonArrLenCommandCodec.encode(charset = cfg.charset, key = key, path = path)
    }
    return JsonArrLenCommandCodec.decode(topology.handle(request), cfg.charset)
}
