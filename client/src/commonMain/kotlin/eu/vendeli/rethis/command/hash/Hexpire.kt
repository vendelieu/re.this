package eu.vendeli.rethis.command.hash

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.common.UpdateStrategyOption
import eu.vendeli.rethis.codecs.hash.HExpireCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlin.time.Duration

public suspend fun ReThis.hExpire(
    key: String,
    seconds: Duration,
    vararg `field`: String,
    condition: UpdateStrategyOption? = null,
): List<Long> {
    val request = if(cfg.withSlots) {
        HExpireCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, seconds = seconds, field = field, condition = condition)
    } else {
        HExpireCommandCodec.encode(charset = cfg.charset, key = key, seconds = seconds, field = field, condition = condition)
    }
    return HExpireCommandCodec.decode(topology.handle(request), cfg.charset)
}
