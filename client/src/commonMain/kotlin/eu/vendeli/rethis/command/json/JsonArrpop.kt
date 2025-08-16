package eu.vendeli.rethis.command.json

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.codecs.json.JsonArrPopCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.jsonArrPop(
    key: String,
    path: String? = null,
    index: Long? = null,
): RType {
    val request = if(cfg.withSlots) {
        JsonArrPopCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, path = path, index = index)
    } else {
        JsonArrPopCommandCodec.encode(charset = cfg.charset, key = key, path = path, index = index)
    }
    return JsonArrPopCommandCodec.decode(topology.handle(request), cfg.charset)
}
