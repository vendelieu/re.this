package eu.vendeli.rethis.command.connection

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.connection.ClientListCommandCodec
import eu.vendeli.rethis.shared.request.connection.ClientType
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.clientList(clientType: ClientType? = null, vararg clientId: Long): String {
    val request = if(cfg.withSlots) {
        ClientListCommandCodec.encodeWithSlot(charset = cfg.charset, clientType = clientType, clientId = clientId)
    } else {
        ClientListCommandCodec.encode(charset = cfg.charset, clientType = clientType, clientId = clientId)
    }
    return ClientListCommandCodec.decode(topology.handle(request), cfg.charset)
}
