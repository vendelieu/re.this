package eu.vendeli.rethis.command.string

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.string.IncrByFloatCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Double
import kotlin.String

public suspend fun ReThis.incrByFloat(key: String, increment: Double): Double {
    val request = if(cfg.withSlots) {
        IncrByFloatCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, increment = increment)
    } else {
        IncrByFloatCommandCodec.encode(charset = cfg.charset, key = key, increment = increment)
    }
    return IncrByFloatCommandCodec.decode(topology.handle(request), cfg.charset)
}
