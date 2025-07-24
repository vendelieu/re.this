package eu.vendeli.rethis.command.json

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.json.JsonDelCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.jsonDel(key: String, path: String? = null): Long {
    val request = if(cfg.withSlots) {
        JsonDelCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, path = path)
    } else {
        JsonDelCommandCodec.encode(charset = cfg.charset, key = key, path = path)
    }
    return JsonDelCommandCodec.decode(topology.handle(request), cfg.charset)
}
