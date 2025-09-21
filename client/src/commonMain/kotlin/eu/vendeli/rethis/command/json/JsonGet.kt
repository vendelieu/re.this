package eu.vendeli.rethis.command.json

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.json.JsonGetCommandCodec
import eu.vendeli.rethis.shared.request.json.JsonGetOption
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.jsonGet(key: String, vararg options: JsonGetOption): String? {
    val request = if(cfg.withSlots) {
        JsonGetCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, options = options)
    } else {
        JsonGetCommandCodec.encode(charset = cfg.charset, key = key, options = options)
    }
    return JsonGetCommandCodec.decode(topology.handle(request), cfg.charset)
}
