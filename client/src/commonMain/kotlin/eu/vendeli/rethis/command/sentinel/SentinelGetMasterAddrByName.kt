package eu.vendeli.rethis.command.sentinel

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sentinel.SentinelGetMasterAddrCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.sentinelGetMasterAddr(masterName: String): List<String> {
    val request = if(cfg.withSlots) {
        SentinelGetMasterAddrCommandCodec.encodeWithSlot(charset = cfg.charset, masterName = masterName)
    } else {
        SentinelGetMasterAddrCommandCodec.encode(charset = cfg.charset, masterName = masterName)
    }
    return SentinelGetMasterAddrCommandCodec.decode(topology.handle(request), cfg.charset)
}
