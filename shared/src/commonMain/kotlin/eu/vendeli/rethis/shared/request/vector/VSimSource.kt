package eu.vendeli.rethis.shared.request.vector

import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption

sealed class VSimSource {
    @RedisOption.Token("ELE")
    class Ele(val element: String) : VSimSource()

    @RedisOption.Token("FP32")
    class Fp32(val blob: ByteArray) : VSimSource()

    @RedisOption.Token("VALUES")
    class Values(
        @RedisMeta.WithSizeParam("num") vararg val components: Double,
    ) : VSimSource()
}
