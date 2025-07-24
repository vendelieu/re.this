package eu.vendeli.rethis.command.json

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.json.JsonNumIncrByCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.jsonNumIncrBy(
    key: String,
    path: String,
    `value`: Double,
): List<Long?> {
    val request = if(cfg.withSlots) {
        JsonNumIncrByCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, path = path, value = value)
    } else {
        JsonNumIncrByCommandCodec.encode(charset = cfg.charset, key = key, path = path, value = value)
    }
    return JsonNumIncrByCommandCodec.decode(topology.handle(request), cfg.charset)
}
