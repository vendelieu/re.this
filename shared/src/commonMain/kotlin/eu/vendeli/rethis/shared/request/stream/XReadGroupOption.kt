package eu.vendeli.rethis.shared.request.stream

import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.types.TimeUnit
import kotlin.time.Duration


sealed class XReadGroupOption {
    @RedisOption.Token("COUNT")
    class Count(val count: Long) : XReadGroupOption()

    @RedisOption.Token("BLOCK")
    class Block(val milliseconds: @RedisMeta.OutgoingTimeUnit(TimeUnit.MILLISECONDS) Duration) : XReadGroupOption()

    @RedisOption.Token("NOACK")
    data object NoAck : XReadGroupOption()
}

@RedisOption.Token("STREAMS")
class XReadGroupKeyIds(
    val key: List<String>,
    val id: List<String>,
)
