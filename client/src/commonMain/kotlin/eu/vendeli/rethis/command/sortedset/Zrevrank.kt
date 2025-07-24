package eu.vendeli.rethis.command.sortedset

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sortedset.ZRevRankCommandCodec
import eu.vendeli.rethis.codecs.sortedset.ZRevRankWithScoreCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.zRevRankWithScore(
    key: String,
    member: String,
    withScore: Boolean,
): List<Long>? {
    val request = if(cfg.withSlots) {
        ZRevRankWithScoreCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, member = member, withScore = withScore)
    } else {
        ZRevRankWithScoreCommandCodec.encode(charset = cfg.charset, key = key, member = member, withScore = withScore)
    }
    return ZRevRankWithScoreCommandCodec.decode(topology.handle(request), cfg.charset)
}

public suspend fun ReThis.zRevRank(key: String, member: String): Long? {
    val request = if(cfg.withSlots) {
        ZRevRankCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, member = member)
    } else {
        ZRevRankCommandCodec.encode(charset = cfg.charset, key = key, member = member)
    }
    return ZRevRankCommandCodec.decode(topology.handle(request), cfg.charset)
}
