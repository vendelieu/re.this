package eu.vendeli.rethis.command.json

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.codecs.json.JsonRespCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.jsonResp(key: String, path: String? = null): List<RType> {
    val request = if(cfg.withSlots) {
        JsonRespCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, path = path)
    } else {
        JsonRespCommandCodec.encode(charset = cfg.charset, key = key, path = path)
    }
    return JsonRespCommandCodec.decode(topology.handle(request), cfg.charset)
}
