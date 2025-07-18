package eu.vendeli.rethis.command.`set`

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.`set`.SRandMemberCommandCodec
import eu.vendeli.rethis.codecs.`set`.SRandMemberCountCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String
import kotlin.collections.List

public suspend fun ReThis.sRandMemberCount(key: String, count: Long? = null): List<String> {
    val request = if(cfg.withSlots) {
        SRandMemberCountCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, count = count)
    } else {
        SRandMemberCountCommandCodec.encode(charset = cfg.charset, key = key, count = count)
    }
    return SRandMemberCountCommandCodec.decode(topology.handle(request), cfg.charset)
}

public suspend fun ReThis.sRandMember(key: String): String {
    val request = if(cfg.withSlots) {
        SRandMemberCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        SRandMemberCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return SRandMemberCommandCodec.decode(topology.handle(request), cfg.charset)
}
