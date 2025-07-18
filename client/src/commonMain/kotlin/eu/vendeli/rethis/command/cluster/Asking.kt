package eu.vendeli.rethis.command.cluster

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.cluster.AskingCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean

public suspend fun ReThis.asking(): Boolean {
    val request = if(cfg.withSlots) {
        AskingCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        AskingCommandCodec.encode(charset = cfg.charset, )
    }
    return AskingCommandCodec.decode(topology.handle(request), cfg.charset)
}
