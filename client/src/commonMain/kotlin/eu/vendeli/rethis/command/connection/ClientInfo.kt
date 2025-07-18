package eu.vendeli.rethis.command.connection

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.connection.ClientInfoCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.String

public suspend fun ReThis.clientInfo(): String {
    val request = if(cfg.withSlots) {
        ClientInfoCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        ClientInfoCommandCodec.encode(charset = cfg.charset, )
    }
    return ClientInfoCommandCodec.decode(topology.handle(request), cfg.charset)
}
