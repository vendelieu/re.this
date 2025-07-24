package eu.vendeli.rethis.command.hash

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.common.UpdateStrategyOption
import eu.vendeli.rethis.codecs.hash.HExpireAtCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.time.Instant

public suspend fun ReThis.hExpireAt(
    key: String,
    unixTimeSeconds: Instant,
    vararg `field`: String,
    condition: UpdateStrategyOption? = null,
): List<Long> {
    val request = if(cfg.withSlots) {
        HExpireAtCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, unixTimeSeconds = unixTimeSeconds, field = field, condition = condition)
    } else {
        HExpireAtCommandCodec.encode(charset = cfg.charset, key = key, unixTimeSeconds = unixTimeSeconds, field = field, condition = condition)
    }
    return HExpireAtCommandCodec.decode(topology.handle(request), cfg.charset)
}
