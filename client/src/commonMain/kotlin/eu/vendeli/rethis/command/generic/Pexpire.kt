package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.shared.request.common.UpdateStrategyOption
import eu.vendeli.rethis.codecs.generic.PExpireCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.time.Duration

public suspend fun ReThis.pExpire(
    key: String,
    milliseconds: Duration,
    condition: UpdateStrategyOption? = null,
): Boolean {
    val request = if(cfg.withSlots) {
        PExpireCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, milliseconds = milliseconds, condition = condition)
    } else {
        PExpireCommandCodec.encode(charset = cfg.charset, key = key, milliseconds = milliseconds, condition = condition)
    }
    return PExpireCommandCodec.decode(topology.handle(request), cfg.charset)
}
