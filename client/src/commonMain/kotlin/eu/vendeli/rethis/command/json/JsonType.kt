package eu.vendeli.rethis.command.json

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.json.JsonTypeCommandCodec
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.jsonType(key: String, path: String? = null): RType {
    val request = if (cfg.withSlots) {
        JsonTypeCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, path = path)
    } else {
        JsonTypeCommandCodec.encode(charset = cfg.charset, key = key, path = path)
    }
    return JsonTypeCommandCodec.decode(topology.handle(request), cfg.charset)
}
