package eu.vendeli.rethis.command.json

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.json.JsonObjLenCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String
import kotlin.collections.List

public suspend fun ReThis.jsonObjLen(key: String, path: String? = null): List<Long?> {
    val request = if(cfg.withSlots) {
        JsonObjLenCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, path = path)
    } else {
        JsonObjLenCommandCodec.encode(charset = cfg.charset, key = key, path = path)
    }
    return JsonObjLenCommandCodec.decode(topology.handle(request), cfg.charset)
}
