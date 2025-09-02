package eu.vendeli.rethis.command.stream

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.stream.XSetIdCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.xSetId(
    key: String,
    lastId: String,
    entriesAdded: Long? = null,
    maxDeletedId: String? = null,
): Boolean {
    val request = if(cfg.withSlots) {
        XSetIdCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, lastId = lastId, entriesAdded = entriesAdded, maxDeletedId = maxDeletedId)
    } else {
        XSetIdCommandCodec.encode(charset = cfg.charset, key = key, lastId = lastId, entriesAdded = entriesAdded, maxDeletedId = maxDeletedId)
    }
    return XSetIdCommandCodec.decode(topology.handle(request), cfg.charset)
}
