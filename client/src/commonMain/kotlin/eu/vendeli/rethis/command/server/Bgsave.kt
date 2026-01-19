package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.BgSaveCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.bgSave(schedule: Boolean? = null): Boolean {
    val request = if (cfg.withSlots) {
        BgSaveCommandCodec.encodeWithSlot(charset = cfg.charset, schedule = schedule)
    } else {
        BgSaveCommandCodec.encode(charset = cfg.charset, schedule = schedule)
    }
    return BgSaveCommandCodec.decode(topology.handle(request), cfg.charset)
}
