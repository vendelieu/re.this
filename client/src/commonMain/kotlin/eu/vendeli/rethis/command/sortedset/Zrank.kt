package eu.vendeli.rethis.command.sortedset

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.codecs.sortedset.ZRankCommandCodec
import eu.vendeli.rethis.codecs.sortedset.ZRankWithScoresCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.zRankWithScores(
    key: String,
    member: String,
    withScore: Boolean? = null,
): List<RType>? {
    val request = if(cfg.withSlots) {
        ZRankWithScoresCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, member = member, withScore = withScore)
    } else {
        ZRankWithScoresCommandCodec.encode(charset = cfg.charset, key = key, member = member, withScore = withScore)
    }
    return ZRankWithScoresCommandCodec.decode(topology.handle(request), cfg.charset)
}

public suspend fun ReThis.zRank(key: String, member: String): Long? {
    val request = if(cfg.withSlots) {
        ZRankCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, member = member)
    } else {
        ZRankCommandCodec.encode(charset = cfg.charset, key = key, member = member)
    }
    return ZRankCommandCodec.decode(topology.handle(request), cfg.charset)
}
