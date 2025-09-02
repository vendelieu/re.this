package eu.vendeli.rethis.shared.request.bitmap

import eu.vendeli.rethis.shared.annotations.RedisOption

sealed class BitfieldOption {
    @RedisOption.Token("GET")
    class Get(
        val encoding: String,
        val offset: Long,
    ) : BitfieldOption()

    @RedisOption.Token("OVERFLOW")
    class Overflow(
        @RedisOption.Name("overflowBlock") val type: Type,
    ) : BitfieldOption() {
        enum class Type { WRAP, SAT, FAIL }
    }

    @RedisOption.Token("SET")
    class Set(
        val encoding: String,
        val offset: Long,
        val value: Long,
    ) : BitfieldOption()

    @RedisOption.Token("INCRBY")
    class IncreaseBy(
        val encoding: String,
        val offset: Long,
        val increment: Long,
    ) : BitfieldOption()
}
