package eu.vendeli.rethis.command.json

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.json.JsonToggleCommandCodec
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.jsonToggle(key: String, path: String): RType {
    val request = if(cfg.withSlots) {
        JsonToggleCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, path = path)
    } else {
        JsonToggleCommandCodec.encode(charset = cfg.charset, key = key, path = path)
    }
    return JsonToggleCommandCodec.decode(topology.handle(request), cfg.charset)
}
