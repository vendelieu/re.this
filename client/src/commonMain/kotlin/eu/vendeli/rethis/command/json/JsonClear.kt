package eu.vendeli.rethis.command.json

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.json.JsonClearCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String

public suspend fun ReThis.jsonClear(key: String, path: String? = null): Long {
    val request = if(cfg.withSlots) {
        JsonClearCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, path = path)
    } else {
        JsonClearCommandCodec.encode(charset = cfg.charset, key = key, path = path)
    }
    return JsonClearCommandCodec.decode(topology.handle(request), cfg.charset)
}
