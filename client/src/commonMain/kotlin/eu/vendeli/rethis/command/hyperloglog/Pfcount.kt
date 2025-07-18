package eu.vendeli.rethis.command.hyperloglog

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.hyperloglog.PfCountCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String

public suspend fun ReThis.pfCount(vararg key: String): Long {
    val request = if(cfg.withSlots) {
        PfCountCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        PfCountCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return PfCountCommandCodec.decode(topology.handle(request), cfg.charset)
}
