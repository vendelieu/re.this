package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.decoders.ModuleOptionArgsEncoder

sealed class ModuleOption {
    @RedisOption
    @RedisMeta.OrderPriority(0)
    data class CONFIG(val name: String, val value: String) : ModuleOption()

    @RedisOption
    @RedisMeta.CustomCodec(encoder = ModuleOptionArgsEncoder::class)
    @RedisMeta.OrderPriority(1)
    data class ARGS(val args: List<String>) : ModuleOption()
}
