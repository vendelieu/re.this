package eu.vendeli.rethis.command.json

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.json.JsonMGetCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.String
import kotlin.collections.List

public suspend fun ReThis.jsonMGet(path: String, vararg key: String): List<String?> {
    val request = if(cfg.withSlots) {
        JsonMGetCommandCodec.encodeWithSlot(charset = cfg.charset, path = path, key = key)
    } else {
        JsonMGetCommandCodec.encode(charset = cfg.charset, path = path, key = key)
    }
    return JsonMGetCommandCodec.decode(topology.handle(request), cfg.charset)
}
