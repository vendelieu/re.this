package eu.vendeli.rethis.command.json

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.json.JsonMergeCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.jsonMerge(
    key: String,
    path: String,
    `value`: String,
): Boolean {
    val request = if (cfg.withSlots) {
        JsonMergeCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, path = path, value = value)
    } else {
        JsonMergeCommandCodec.encode(charset = cfg.charset, key = key, path = path, value = value)
    }
    return JsonMergeCommandCodec.decode(topology.handle(request), cfg.charset)
}
