package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.common.UpdateStrategyOption
import eu.vendeli.rethis.codecs.generic.ExpireAtCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean
import kotlin.String
import kotlin.time.Instant

public suspend fun ReThis.expireAt(
    key: String,
    unixTimeSeconds: Instant,
    condition: UpdateStrategyOption? = null,
): Boolean {
    val request = if(cfg.withSlots) {
        ExpireAtCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, unixTimeSeconds = unixTimeSeconds, condition = condition)
    } else {
        ExpireAtCommandCodec.encode(charset = cfg.charset, key = key, unixTimeSeconds = unixTimeSeconds, condition = condition)
    }
    return ExpireAtCommandCodec.decode(topology.handle(request), cfg.charset)
}
