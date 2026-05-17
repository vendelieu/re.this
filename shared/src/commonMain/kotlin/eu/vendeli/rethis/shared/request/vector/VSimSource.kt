package eu.vendeli.rethis.shared.request.vector

import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption

sealed class VSimSource {
    @RedisOption.Token("ELE")
    class Ele(@RedisOption.Name("vector_or_element") val element: String) : VSimSource()

    @RedisOption.Token("FP32")
    class Fp32(@RedisOption.Name("vector_or_element") val blob: ByteArray) : VSimSource()

    @RedisOption.Token("VALUES")
    class Values(
        @RedisMeta.WithSizeParam("num")
        @RedisOption.Name("vector_or_element")
        vararg val components: Double,
    ) : VSimSource()
}
