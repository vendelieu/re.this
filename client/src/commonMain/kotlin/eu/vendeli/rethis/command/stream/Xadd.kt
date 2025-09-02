package eu.vendeli.rethis.command.stream

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.shared.request.common.FieldValue
import eu.vendeli.rethis.shared.request.stream.XAddOption
import eu.vendeli.rethis.codecs.stream.XAddCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.xAdd(
    key: String,
    nomkstream: Boolean? = null,
    trim: XAddOption.Trim? = null,
    idSelector: XAddOption.Identifier,
    vararg `data`: FieldValue,
): String? {
    val request = if(cfg.withSlots) {
        XAddCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, nomkstream = nomkstream, trim = trim, idSelector = idSelector, data = data)
    } else {
        XAddCommandCodec.encode(charset = cfg.charset, key = key, nomkstream = nomkstream, trim = trim, idSelector = idSelector, data = data)
    }
    return XAddCommandCodec.decode(topology.handle(request), cfg.charset)
}
