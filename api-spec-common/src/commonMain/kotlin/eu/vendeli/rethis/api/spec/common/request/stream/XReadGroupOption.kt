package eu.vendeli.rethis.api.spec.common.request.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer
import eu.vendeli.rethis.api.spec.common.types.TimeUnit
import kotlin.time.Duration

@RedisOptionContainer
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
) {
    init {
        require(id.size == key.size) { "Keys and Ids should match in size" }
    }
}
