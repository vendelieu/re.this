package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.generic.WaitCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long

public suspend fun ReThis.wait(numreplicas: Long, timeout: Long): Long {
    val request = if(cfg.withSlots) {
        WaitCommandCodec.encodeWithSlot(charset = cfg.charset, numreplicas = numreplicas, timeout = timeout)
    } else {
        WaitCommandCodec.encode(charset = cfg.charset, numreplicas = numreplicas, timeout = timeout)
    }
    return WaitCommandCodec.decode(topology.handle(request), cfg.charset)
}
