package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.MemoryDoctorCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.memoryDoctor(): String {
    val request = if (cfg.withSlots) {
        MemoryDoctorCommandCodec.encodeWithSlot(charset = cfg.charset)
    } else {
        MemoryDoctorCommandCodec.encode(charset = cfg.charset)
    }
    return MemoryDoctorCommandCodec.decode(topology.handle(request), cfg.charset)
}
