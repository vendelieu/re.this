package eu.vendeli.rethis.command.connection

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.connection.ClientSetNameCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.clientSetName(connectionName: String): Boolean {
    val request = if(cfg.withSlots) {
        ClientSetNameCommandCodec.encodeWithSlot(charset = cfg.charset, connectionName = connectionName)
    } else {
        ClientSetNameCommandCodec.encode(charset = cfg.charset, connectionName = connectionName)
    }
    return ClientSetNameCommandCodec.decode(topology.handle(request), cfg.charset)
}
