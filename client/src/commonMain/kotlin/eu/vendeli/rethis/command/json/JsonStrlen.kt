package eu.vendeli.rethis.command.json

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.json.JsonStrLenCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.jsonStrLen(key: String, path: String? = null): Long {
    val request = if (cfg.withSlots) {
        JsonStrLenCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, path = path)
    } else {
        JsonStrLenCommandCodec.encode(charset = cfg.charset, key = key, path = path)
    }
    return JsonStrLenCommandCodec.decode(topology.handle(request), cfg.charset)
}
