package eu.vendeli.rethis.command.stream

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.stream.XId
import eu.vendeli.rethis.codecs.stream.XGroupCreateCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean
import kotlin.Long
import kotlin.String

public suspend fun ReThis.xGroupCreate(
    key: String,
    group: String,
    idSelector: XId,
    mkstream: Boolean? = null,
    entriesread: Long? = null,
): Boolean {
    val request = if(cfg.withSlots) {
        XGroupCreateCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, group = group, idSelector = idSelector, mkstream = mkstream, entriesread = entriesread)
    } else {
        XGroupCreateCommandCodec.encode(charset = cfg.charset, key = key, group = group, idSelector = idSelector, mkstream = mkstream, entriesread = entriesread)
    }
    return XGroupCreateCommandCodec.decode(topology.handle(request), cfg.charset)
}
