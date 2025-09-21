package eu.vendeli.rethis.command.connection

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.connection.ClientTrackingCommandCodec
import eu.vendeli.rethis.shared.request.connection.ClientStandby
import eu.vendeli.rethis.shared.request.connection.ClientTrackingMode
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.clientTracking(status: ClientStandby, vararg options: ClientTrackingMode): Boolean {
    val request = if(cfg.withSlots) {
        ClientTrackingCommandCodec.encodeWithSlot(charset = cfg.charset, status = status, options = options)
    } else {
        ClientTrackingCommandCodec.encode(charset = cfg.charset, status = status, options = options)
    }
    return ClientTrackingCommandCodec.decode(topology.handle(request), cfg.charset)
}
