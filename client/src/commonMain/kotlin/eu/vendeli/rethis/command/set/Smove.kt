package eu.vendeli.rethis.command.`set`

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.set.SMoveCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.sMove(
    source: String,
    destination: String,
    member: String,
): Boolean {
    val request = if(cfg.withSlots) {
        SMoveCommandCodec.encodeWithSlot(charset = cfg.charset, source = source, destination = destination, member = member)
    } else {
        SMoveCommandCodec.encode(charset = cfg.charset, source = source, destination = destination, member = member)
    }
    return SMoveCommandCodec.decode(topology.handle(request), cfg.charset)
}
