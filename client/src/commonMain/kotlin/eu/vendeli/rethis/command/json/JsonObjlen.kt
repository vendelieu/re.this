package eu.vendeli.rethis.command.json

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.codecs.json.JsonObjLenCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.jsonObjLen(key: String, path: String? = null): RType {
    val request = if(cfg.withSlots) {
        JsonObjLenCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, path = path)
    } else {
        JsonObjLenCommandCodec.encode(charset = cfg.charset, key = key, path = path)
    }
    return JsonObjLenCommandCodec.decode(topology.handle(request), cfg.charset)
}
