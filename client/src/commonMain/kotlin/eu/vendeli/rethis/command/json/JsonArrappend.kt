package eu.vendeli.rethis.command.json

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.json.JsonArrAppendCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String

public suspend fun ReThis.jsonArrAppend(
    key: String,
    path: String? = null,
    vararg `value`: String,
): Long {
    val request = if(cfg.withSlots) {
        JsonArrAppendCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, path = path, value = value)
    } else {
        JsonArrAppendCommandCodec.encode(charset = cfg.charset, key = key, path = path, value = value)
    }
    return JsonArrAppendCommandCodec.decode(topology.handle(request), cfg.charset)
}
