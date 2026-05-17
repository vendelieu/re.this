package eu.vendeli.rethis.shared.request.vector

import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption

sealed class VAddInput {
    @RedisOption.Token("FP32")
    class Fp32(@RedisOption.Name("vector") val blob: ByteArray) : VAddInput()

    @RedisOption.Token("VALUES")
    class Values(
        @RedisMeta.WithSizeParam("num")
        @RedisOption.Name("vector")
        vararg val components: Double,
    ) : VAddInput()
}
