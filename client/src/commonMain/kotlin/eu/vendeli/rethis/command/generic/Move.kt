package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.generic.MoveCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.move(key: String, db: Long): Boolean {
    val request = if(cfg.withSlots) {
        MoveCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, db = db)
    } else {
        MoveCommandCodec.encode(charset = cfg.charset, key = key, db = db)
    }
    return MoveCommandCodec.decode(topology.handle(request), cfg.charset)
}
