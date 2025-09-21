package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.generic.ExpireCommandCodec
import eu.vendeli.rethis.shared.request.common.UpdateStrategyOption
import eu.vendeli.rethis.topology.handle
import kotlin.time.Duration

public suspend fun ReThis.expire(
    key: String,
    seconds: Duration,
    condition: UpdateStrategyOption? = null,
): Boolean {
    val request = if(cfg.withSlots) {
        ExpireCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, seconds = seconds, condition = condition)
    } else {
        ExpireCommandCodec.encode(charset = cfg.charset, key = key, seconds = seconds, condition = condition)
    }
    return ExpireCommandCodec.decode(topology.handle(request), cfg.charset)
}
