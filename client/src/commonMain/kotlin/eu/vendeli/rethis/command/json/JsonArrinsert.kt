package eu.vendeli.rethis.command.json

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.json.JsonArrInsertCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.jsonArrInsert(
    key: String,
    path: String,
    index: Long,
    vararg `value`: String,
): Long {
    val request = if (cfg.withSlots) {
        JsonArrInsertCommandCodec.encodeWithSlot(
            charset = cfg.charset,
            key = key,
            path = path,
            index = index,
            value = value,
        )
    } else {
        JsonArrInsertCommandCodec.encode(charset = cfg.charset, key = key, path = path, index = index, value = value)
    }
    return JsonArrInsertCommandCodec.decode(topology.handle(request), cfg.charset)
}
