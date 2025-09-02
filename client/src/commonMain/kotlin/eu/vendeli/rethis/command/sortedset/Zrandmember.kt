package eu.vendeli.rethis.command.sortedset

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sortedset.ZRandMemberCommandCodec
import eu.vendeli.rethis.codecs.sortedset.ZRandMemberCountCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.zRandMemberCount(
    key: String,
    count: Long,
    withScores: Boolean? = null,
): List<String> {
    val request = if(cfg.withSlots) {
        ZRandMemberCountCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, count = count, withScores = withScores)
    } else {
        ZRandMemberCountCommandCodec.encode(charset = cfg.charset, key = key, count = count, withScores = withScores)
    }
    return ZRandMemberCountCommandCodec.decode(topology.handle(request), cfg.charset)
}

public suspend fun ReThis.zRandMember(key: String): String {
    val request = if(cfg.withSlots) {
        ZRandMemberCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        ZRandMemberCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return ZRandMemberCommandCodec.decode(topology.handle(request), cfg.charset)
}
