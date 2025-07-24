package eu.vendeli.rethis.command.json

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.json.JsonForgetCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.jsonForget(key: String, path: String? = null): Long {
    val request = if(cfg.withSlots) {
        JsonForgetCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, path = path)
    } else {
        JsonForgetCommandCodec.encode(charset = cfg.charset, key = key, path = path)
    }
    return JsonForgetCommandCodec.decode(topology.handle(request), cfg.charset)
}
