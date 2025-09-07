package eu.vendeli.rethis.command.bitmap

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.bitmap.BitOpCommandCodec
import eu.vendeli.rethis.shared.request.bitmap.BitOpOption
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.bitOp(
    operation: BitOpOption.OperationType,
    destkey: String,
    vararg key: String,
): Long {
    val request = if(cfg.withSlots) {
        BitOpCommandCodec.encodeWithSlot(charset = cfg.charset, operation = operation, destkey = destkey, key = key)
    } else {
        BitOpCommandCodec.encode(charset = cfg.charset, operation = operation, destkey = destkey, key = key)
    }
    return BitOpCommandCodec.decode(topology.handle(request), cfg.charset)
}
