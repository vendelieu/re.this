package eu.vendeli.rethis.command.json

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.json.JsonObjKeysCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.String
import kotlin.collections.List

public suspend fun ReThis.jsonObjKeys(key: String, path: String? = null): List<String> {
    val request = if(cfg.withSlots) {
        JsonObjKeysCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, path = path)
    } else {
        JsonObjKeysCommandCodec.encode(charset = cfg.charset, key = key, path = path)
    }
    return JsonObjKeysCommandCodec.decode(topology.handle(request), cfg.charset)
}
