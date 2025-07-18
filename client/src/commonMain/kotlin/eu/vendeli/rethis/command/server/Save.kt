package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.SaveCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean

public suspend fun ReThis.save(): Boolean {
    val request = if(cfg.withSlots) {
        SaveCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        SaveCommandCodec.encode(charset = cfg.charset, )
    }
    return SaveCommandCodec.decode(topology.handle(request), cfg.charset)
}
