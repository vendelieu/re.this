package eu.vendeli.rethis.command.hyperloglog

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.hyperloglog.PfMergeCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.pfMerge(destkey: String, vararg sourcekey: String): String {
    val request = if (cfg.withSlots) {
        PfMergeCommandCodec.encodeWithSlot(charset = cfg.charset, destkey = destkey, sourcekey = sourcekey)
    } else {
        PfMergeCommandCodec.encode(charset = cfg.charset, destkey = destkey, sourcekey = sourcekey)
    }
    return PfMergeCommandCodec.decode(topology.handle(request), cfg.charset)
}
