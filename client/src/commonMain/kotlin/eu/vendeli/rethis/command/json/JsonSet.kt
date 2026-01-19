package eu.vendeli.rethis.command.json

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.json.JsonSetCommandCodec
import eu.vendeli.rethis.shared.request.string.UpsertMode
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.jsonSet(
    key: String,
    `value`: String,
    path: String = "$",
    condition: UpsertMode? = null,
): String {
    val request = if (cfg.withSlots) {
        JsonSetCommandCodec.encodeWithSlot(
            charset = cfg.charset,
            key = key,
            value = value,
            path = path,
            condition = condition,
        )
    } else {
        JsonSetCommandCodec.encode(charset = cfg.charset, key = key, value = value, path = path, condition = condition)
    }
    return JsonSetCommandCodec.decode(topology.handle(request), cfg.charset)
}
