package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.MonitorCommandCodec
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.monitor(): RType {
    val request = if (cfg.withSlots) {
        MonitorCommandCodec.encodeWithSlot(charset = cfg.charset)
    } else {
        MonitorCommandCodec.encode(charset = cfg.charset)
    }
    return MonitorCommandCodec.decode(topology.handle(request), cfg.charset)
}
