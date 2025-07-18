package eu.vendeli.rethis.command.json

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.json.JsonStrAppendCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String

public suspend fun ReThis.jsonStrAppend(
    key: String,
    `value`: String,
    path: String? = null,
): Long {
    val request = if(cfg.withSlots) {
        JsonStrAppendCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, value = value, path = path)
    } else {
        JsonStrAppendCommandCodec.encode(charset = cfg.charset, key = key, value = value, path = path)
    }
    return JsonStrAppendCommandCodec.decode(topology.handle(request), cfg.charset)
}
