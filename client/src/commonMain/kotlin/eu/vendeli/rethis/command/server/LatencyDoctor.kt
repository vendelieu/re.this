package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.LatencyDoctorCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.latencyDoctor(): String {
    val request = if(cfg.withSlots) {
        LatencyDoctorCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        LatencyDoctorCommandCodec.encode(charset = cfg.charset, )
    }
    return LatencyDoctorCommandCodec.decode(topology.handle(request), cfg.charset)
}
