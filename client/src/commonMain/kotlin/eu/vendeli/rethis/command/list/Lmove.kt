package eu.vendeli.rethis.command.list

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.list.LMoveCommandCodec
import eu.vendeli.rethis.shared.response.common.MoveDirection
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.lMove(
    source: String,
    destination: String,
    whereFrom: MoveDirection,
    whereTo: MoveDirection,
): String {
    val request = if(cfg.withSlots) {
        LMoveCommandCodec.encodeWithSlot(charset = cfg.charset, source = source, destination = destination, whereFrom = whereFrom, whereTo = whereTo)
    } else {
        LMoveCommandCodec.encode(charset = cfg.charset, source = source, destination = destination, whereFrom = whereFrom, whereTo = whereTo)
    }
    return LMoveCommandCodec.decode(topology.handle(request), cfg.charset)
}
