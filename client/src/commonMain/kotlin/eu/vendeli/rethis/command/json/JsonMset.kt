package eu.vendeli.rethis.command.json

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.json.JsonEntry
import eu.vendeli.rethis.codecs.json.JsonMSetCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.jsonMSet(vararg triplet: JsonEntry): Boolean {
    val request = if(cfg.withSlots) {
        JsonMSetCommandCodec.encodeWithSlot(charset = cfg.charset, triplet = triplet)
    } else {
        JsonMSetCommandCodec.encode(charset = cfg.charset, triplet = triplet)
    }
    return JsonMSetCommandCodec.decode(topology.handle(request), cfg.charset)
}
