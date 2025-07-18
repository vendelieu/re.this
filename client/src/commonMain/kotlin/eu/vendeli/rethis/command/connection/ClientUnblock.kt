package eu.vendeli.rethis.command.connection

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.connection.ClientUnblockType
import eu.vendeli.rethis.codecs.connection.ClientUnblockCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean
import kotlin.Long

public suspend fun ReThis.clientUnblock(clientId: Long, unblockType: ClientUnblockType? = null): Boolean {
    val request = if(cfg.withSlots) {
        ClientUnblockCommandCodec.encodeWithSlot(charset = cfg.charset, clientId = clientId, unblockType = unblockType)
    } else {
        ClientUnblockCommandCodec.encode(charset = cfg.charset, clientId = clientId, unblockType = unblockType)
    }
    return ClientUnblockCommandCodec.decode(topology.handle(request), cfg.charset)
}
