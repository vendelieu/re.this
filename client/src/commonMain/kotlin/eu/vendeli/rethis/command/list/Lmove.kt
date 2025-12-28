package eu.vendeli.rethis.command.list

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.list.LMoveBSCommandCodec
import eu.vendeli.rethis.codecs.list.LMoveCommandCodec
import eu.vendeli.rethis.shared.response.common.MoveDirection
import eu.vendeli.rethis.topology.handle
import kotlinx.io.bytestring.ByteString

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

public suspend fun ReThis.lMoveBS(
    source: String,
    destination: String,
    whereFrom: MoveDirection,
    whereTo: MoveDirection,
): ByteString {
    val request = if(cfg.withSlots) {
        LMoveBSCommandCodec.encodeWithSlot(charset = cfg.charset, source = source, destination = destination, whereFrom = whereFrom, whereTo = whereTo)
    } else {
        LMoveBSCommandCodec.encode(charset = cfg.charset, source = source, destination = destination, whereFrom = whereFrom, whereTo = whereTo)
    }
    return LMoveBSCommandCodec.decode(topology.handle(request), cfg.charset)
}
