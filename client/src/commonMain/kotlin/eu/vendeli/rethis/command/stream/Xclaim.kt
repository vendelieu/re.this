package eu.vendeli.rethis.command.stream

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.stream.XClaimCommandCodec
import eu.vendeli.rethis.shared.request.stream.XClaimOption
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.xClaim(
    key: String,
    group: String,
    consumer: String,
    minIdleTime: String,
    vararg id: String,
    idle: XClaimOption.Idle? = null,
    time: XClaimOption.Time? = null,
    retryCount: XClaimOption.RetryCount? = null,
    force: Boolean? = null,
    justId: Boolean? = null,
    lastId: XClaimOption.LastId? = null,
): List<RType> {
    val request = if(cfg.withSlots) {
        XClaimCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, group = group, consumer = consumer, minIdleTime = minIdleTime, id = id, idle = idle, time = time, retryCount = retryCount, force = force, justId = justId, lastId = lastId)
    } else {
        XClaimCommandCodec.encode(charset = cfg.charset, key = key, group = group, consumer = consumer, minIdleTime = minIdleTime, id = id, idle = idle, time = time, retryCount = retryCount, force = force, justId = justId, lastId = lastId)
    }
    return XClaimCommandCodec.decode(topology.handle(request), cfg.charset)
}
