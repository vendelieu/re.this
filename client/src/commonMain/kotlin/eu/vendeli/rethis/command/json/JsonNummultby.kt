package eu.vendeli.rethis.command.json

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.codecs.json.JsonNumMultByCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.jsonNumMultBy(
    key: String,
    path: String,
    `value`: Double,
): RType {
    val request = if(cfg.withSlots) {
        JsonNumMultByCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, path = path, value = value)
    } else {
        JsonNumMultByCommandCodec.encode(charset = cfg.charset, key = key, path = path, value = value)
    }
    return JsonNumMultByCommandCodec.decode(topology.handle(request), cfg.charset)
}
