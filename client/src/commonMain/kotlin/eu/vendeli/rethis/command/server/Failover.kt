package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.server.FailoverOptions
import eu.vendeli.rethis.codecs.server.FailoverCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.failover(vararg option: FailoverOptions): Boolean {
    val request = if(cfg.withSlots) {
        FailoverCommandCodec.encodeWithSlot(charset = cfg.charset, option = option)
    } else {
        FailoverCommandCodec.encode(charset = cfg.charset, option = option)
    }
    return FailoverCommandCodec.decode(topology.handle(request), cfg.charset)
}
