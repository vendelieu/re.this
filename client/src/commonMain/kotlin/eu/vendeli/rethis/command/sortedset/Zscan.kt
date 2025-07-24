package eu.vendeli.rethis.command.sortedset

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.response.common.ScanResult
import eu.vendeli.rethis.codecs.sortedset.ZScanCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.zScan(
    key: String,
    cursor: Long,
    pattern: String? = null,
    count: Long? = null,
): ScanResult<Pair<String, String>> {
    val request = if(cfg.withSlots) {
        ZScanCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, cursor = cursor, pattern = pattern, count = count)
    } else {
        ZScanCommandCodec.encode(charset = cfg.charset, key = key, cursor = cursor, pattern = pattern, count = count)
    }
    return ZScanCommandCodec.decode(topology.handle(request), cfg.charset)
}
