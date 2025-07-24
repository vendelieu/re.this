package eu.vendeli.rethis.command.stream

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.codecs.stream.XInfoGroupsCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.xInfoGroups(key: String): List<RType> {
    val request = if(cfg.withSlots) {
        XInfoGroupsCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        XInfoGroupsCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return XInfoGroupsCommandCodec.decode(topology.handle(request), cfg.charset)
}
