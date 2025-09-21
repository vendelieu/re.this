package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.generic.PExpireAtCommandCodec
import eu.vendeli.rethis.shared.request.common.UpdateStrategyOption
import eu.vendeli.rethis.topology.handle
import kotlin.time.Instant

public suspend fun ReThis.pExpireAt(
    key: String,
    unixTimeMilliseconds: Instant,
    condition: UpdateStrategyOption? = null,
): Boolean {
    val request = if(cfg.withSlots) {
        PExpireAtCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, unixTimeMilliseconds = unixTimeMilliseconds, condition = condition)
    } else {
        PExpireAtCommandCodec.encode(charset = cfg.charset, key = key, unixTimeMilliseconds = unixTimeMilliseconds, condition = condition)
    }
    return PExpireAtCommandCodec.decode(topology.handle(request), cfg.charset)
}
