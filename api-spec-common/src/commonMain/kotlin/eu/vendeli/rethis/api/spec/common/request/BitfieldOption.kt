package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer

@RedisOptionContainer
sealed class BitfieldOption {
    @RedisOption
    class GET(
        encoding: String,
        offset: Long,
    ) : BitfieldOption()

    @RedisOption
    class OVERFLOW(
        type: Type,
    ) : BitfieldOption() {
        enum class Type { WRAP, SAT, FAIL }
    }

    @RedisOption
    class SET(
        encoding: String,
        offset: Long,
        value: Long,
    ) : BitfieldOption()

    @RedisOption
    class INCRBY(
        encoding: String,
        offset: Long,
        increment: Long,
    ) : BitfieldOption()
}
