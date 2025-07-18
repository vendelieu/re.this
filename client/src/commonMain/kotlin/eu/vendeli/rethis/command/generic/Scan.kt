package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.generic.ScanOption
import eu.vendeli.rethis.api.spec.common.response.ScanResult
import eu.vendeli.rethis.codecs.generic.ScanCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String

public suspend fun ReThis.scan(cursor: Long, vararg option: ScanOption): ScanResult<String> {
    val request = if(cfg.withSlots) {
        ScanCommandCodec.encodeWithSlot(charset = cfg.charset, cursor = cursor, option = option)
    } else {
        ScanCommandCodec.encode(charset = cfg.charset, cursor = cursor, option = option)
    }
    return ScanCommandCodec.decode(topology.handle(request), cfg.charset)
}
