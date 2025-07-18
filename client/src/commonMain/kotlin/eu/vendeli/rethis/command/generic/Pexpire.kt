package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.common.UpdateStrategyOption
import eu.vendeli.rethis.codecs.generic.PExpireCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean
import kotlin.Long
import kotlin.String

public suspend fun ReThis.pExpire(
    key: String,
    milliseconds: Long,
    condition: UpdateStrategyOption? = null,
): Boolean {
    val request = if(cfg.withSlots) {
        PExpireCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, milliseconds = milliseconds, condition = condition)
    } else {
        PExpireCommandCodec.encode(charset = cfg.charset, key = key, milliseconds = milliseconds, condition = condition)
    }
    return PExpireCommandCodec.decode(topology.handle(request), cfg.charset)
}
