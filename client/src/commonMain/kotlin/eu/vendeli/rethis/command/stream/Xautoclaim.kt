package eu.vendeli.rethis.command.stream

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.codecs.stream.XAutoClaimCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlin.collections.List

public suspend fun ReThis.xAutoClaim(
    key: String,
    group: String,
    consumer: String,
    minIdleTime: String,
    start: String,
    count: Long? = null,
    justid: Boolean? = null,
): List<RType> {
    val request = if(cfg.withSlots) {
        XAutoClaimCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, group = group, consumer = consumer, minIdleTime = minIdleTime, start = start, count = count, justid = justid)
    } else {
        XAutoClaimCommandCodec.encode(charset = cfg.charset, key = key, group = group, consumer = consumer, minIdleTime = minIdleTime, start = start, count = count, justid = justid)
    }
    return XAutoClaimCommandCodec.decode(topology.handle(request), cfg.charset)
}
