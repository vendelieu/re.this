package eu.vendeli.rethis.command.connection

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.shared.request.connection.ClientReplyMode
import eu.vendeli.rethis.codecs.connection.ClientReplyCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.clientReply(action: ClientReplyMode): Boolean {
    val request = if(cfg.withSlots) {
        ClientReplyCommandCodec.encodeWithSlot(charset = cfg.charset, action = action)
    } else {
        ClientReplyCommandCodec.encode(charset = cfg.charset, action = action)
    }
    return ClientReplyCommandCodec.decode(topology.handle(request), cfg.charset)
}
