package eu.vendeli.rethis.command.`set`

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.`set`.SUnionStoreCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String

public suspend fun ReThis.sUnionStore(destination: String, vararg key: String): Long {
    val request = if(cfg.withSlots) {
        SUnionStoreCommandCodec.encodeWithSlot(charset = cfg.charset, destination = destination, key = key)
    } else {
        SUnionStoreCommandCodec.encode(charset = cfg.charset, destination = destination, key = key)
    }
    return SUnionStoreCommandCodec.decode(topology.handle(request), cfg.charset)
}
