package eu.vendeli.rethis.command.sortedset

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sortedset.ZDiffCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.zDiff(vararg key: String, withscores: Boolean? = null): List<String> {
    val request = if (cfg.withSlots) {
        ZDiffCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, withscores = withscores)
    } else {
        ZDiffCommandCodec.encode(charset = cfg.charset, key = key, withscores = withscores)
    }
    return ZDiffCommandCodec.decode(topology.handle(request), cfg.charset)
}
