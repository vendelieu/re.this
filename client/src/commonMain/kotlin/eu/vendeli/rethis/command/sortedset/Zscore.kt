package eu.vendeli.rethis.command.sortedset

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sortedset.ZScoreCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.zScore(key: String, member: String): Double? {
    val request = if(cfg.withSlots) {
        ZScoreCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, member = member)
    } else {
        ZScoreCommandCodec.encode(charset = cfg.charset, key = key, member = member)
    }
    return ZScoreCommandCodec.decode(topology.handle(request), cfg.charset)
}
