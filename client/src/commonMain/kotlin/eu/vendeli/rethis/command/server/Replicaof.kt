package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.server.ReplicaOfArgs
import eu.vendeli.rethis.codecs.server.ReplicaOfCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean

public suspend fun ReThis.replicaOf(args: ReplicaOfArgs): Boolean {
    val request = if(cfg.withSlots) {
        ReplicaOfCommandCodec.encodeWithSlot(charset = cfg.charset, args = args)
    } else {
        ReplicaOfCommandCodec.encode(charset = cfg.charset, args = args)
    }
    return ReplicaOfCommandCodec.decode(topology.handle(request), cfg.charset)
}
