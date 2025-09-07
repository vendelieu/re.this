package eu.vendeli.rethis.command.stream

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.stream.XGroupSetIdCommandCodec
import eu.vendeli.rethis.shared.request.stream.XId
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.xGroupSetId(
    key: String,
    group: String,
    idSelector: XId,
    entriesread: Long? = null,
): Boolean {
    val request = if(cfg.withSlots) {
        XGroupSetIdCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, group = group, idSelector = idSelector, entriesread = entriesread)
    } else {
        XGroupSetIdCommandCodec.encode(charset = cfg.charset, key = key, group = group, idSelector = idSelector, entriesread = entriesread)
    }
    return XGroupSetIdCommandCodec.decode(topology.handle(request), cfg.charset)
}
