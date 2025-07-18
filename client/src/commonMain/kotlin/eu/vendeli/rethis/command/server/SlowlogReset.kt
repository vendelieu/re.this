package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.SlowLogResetCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean

public suspend fun ReThis.slowLogReset(): Boolean {
    val request = if(cfg.withSlots) {
        SlowLogResetCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        SlowLogResetCommandCodec.encode(charset = cfg.charset, )
    }
    return SlowLogResetCommandCodec.decode(topology.handle(request), cfg.charset)
}
