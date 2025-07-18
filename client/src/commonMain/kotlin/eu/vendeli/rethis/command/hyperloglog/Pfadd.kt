package eu.vendeli.rethis.command.hyperloglog

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.hyperloglog.PfAddCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean
import kotlin.String

public suspend fun ReThis.pfAdd(key: String, vararg element: String): Boolean {
    val request = if(cfg.withSlots) {
        PfAddCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, element = element)
    } else {
        PfAddCommandCodec.encode(charset = cfg.charset, key = key, element = element)
    }
    return PfAddCommandCodec.decode(topology.handle(request), cfg.charset)
}
