package eu.vendeli.rethis.command.`set`

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.`set`.SScanOption
import eu.vendeli.rethis.api.spec.common.response.ScanResult
import eu.vendeli.rethis.codecs.`set`.SScanCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String

public suspend fun ReThis.sScan(
    key: String,
    cursor: Long,
    vararg option: SScanOption,
): ScanResult<String> {
    val request = if(cfg.withSlots) {
        SScanCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, cursor = cursor, option = option)
    } else {
        SScanCommandCodec.encode(charset = cfg.charset, key = key, cursor = cursor, option = option)
    }
    return SScanCommandCodec.decode(topology.handle(request), cfg.charset)
}
