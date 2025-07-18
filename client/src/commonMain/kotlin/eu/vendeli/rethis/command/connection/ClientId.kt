package eu.vendeli.rethis.command.connection

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.connection.ClientIdCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long

public suspend fun ReThis.clientId(): Long {
    val request = if(cfg.withSlots) {
        ClientIdCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        ClientIdCommandCodec.encode(charset = cfg.charset, )
    }
    return ClientIdCommandCodec.decode(topology.handle(request), cfg.charset)
}
