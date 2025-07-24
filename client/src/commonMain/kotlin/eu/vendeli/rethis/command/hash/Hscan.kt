package eu.vendeli.rethis.command.hash

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.hash.HScanOption
import eu.vendeli.rethis.api.spec.common.response.common.ScanResult
import eu.vendeli.rethis.codecs.hash.HScanCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.hScan(
    key: String,
    cursor: Long,
    vararg option: HScanOption,
): ScanResult<Pair<String, String>> {
    val request = if(cfg.withSlots) {
        HScanCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, cursor = cursor, option = option)
    } else {
        HScanCommandCodec.encode(charset = cfg.charset, key = key, cursor = cursor, option = option)
    }
    return HScanCommandCodec.decode(topology.handle(request), cfg.charset)
}
