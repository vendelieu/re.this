package eu.vendeli.rethis.command.list

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.list.BlMoveBACommandCodec
import eu.vendeli.rethis.codecs.list.BlMoveCommandCodec
import eu.vendeli.rethis.shared.response.common.MoveDirection
import eu.vendeli.rethis.topology.handle

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

public suspend fun ReThis.blMoveBA(
    source: String,
    destination: String,
    whereFrom: MoveDirection,
    whereTo: MoveDirection,
    timeout: Double,
): ByteArray? {
    val request = if(cfg.withSlots) {
        BlMoveBACommandCodec.encodeWithSlot(charset = cfg.charset, source = source, destination = destination, whereFrom = whereFrom, whereTo = whereTo, timeout = timeout)
    } else {
        BlMoveBACommandCodec.encode(charset = cfg.charset, source = source, destination = destination, whereFrom = whereFrom, whereTo = whereTo, timeout = timeout)
    }
    return BlMoveBACommandCodec.decode(topology.handle(request), cfg.charset)
}
