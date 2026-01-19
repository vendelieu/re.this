package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.BgRewriteAofCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.bgRewriteAof(): Boolean {
    val request = if (cfg.withSlots) {
        BgRewriteAofCommandCodec.encodeWithSlot(charset = cfg.charset)
    } else {
        BgRewriteAofCommandCodec.encode(charset = cfg.charset)
    }
    return BgRewriteAofCommandCodec.decode(topology.handle(request), cfg.charset)
}
