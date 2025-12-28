package eu.vendeli.rethis.command.list

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.list.BlMoveBSCommandCodec
import eu.vendeli.rethis.codecs.list.BlMoveCommandCodec
import eu.vendeli.rethis.shared.response.common.MoveDirection
import eu.vendeli.rethis.topology.handle
import kotlinx.io.bytestring.ByteString

public suspend fun ReThis.blMove(
    source: String,
    destination: String,
    whereFrom: MoveDirection,
    whereTo: MoveDirection,
    timeout: Double,
): String? {
    val request = if(cfg.withSlots) {
        BlMoveCommandCodec.encodeWithSlot(charset = cfg.charset, source = source, destination = destination, whereFrom = whereFrom, whereTo = whereTo, timeout = timeout)
    } else {
        BlMoveCommandCodec.encode(charset = cfg.charset, source = source, destination = destination, whereFrom = whereFrom, whereTo = whereTo, timeout = timeout)
    }
    return BlMoveCommandCodec.decode(topology.handle(request), cfg.charset)
}

public suspend fun ReThis.blMoveBS(
    source: String,
    destination: String,
    whereFrom: MoveDirection,
    whereTo: MoveDirection,
    timeout: Double,
): ByteString? {
    val request = if(cfg.withSlots) {
        BlMoveBSCommandCodec.encodeWithSlot(charset = cfg.charset, source = source, destination = destination, whereFrom = whereFrom, whereTo = whereTo, timeout = timeout)
    } else {
        BlMoveBSCommandCodec.encode(charset = cfg.charset, source = source, destination = destination, whereFrom = whereFrom, whereTo = whereTo, timeout = timeout)
    }
    return BlMoveBSCommandCodec.decode(topology.handle(request), cfg.charset)
}
