package eu.vendeli.rethis.api.spec.common.request.server

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer

@RedisOptionContainer
sealed class ModuleOption {
    @RedisOption.Token("CONFIG")
    class Configs(val name: String, val value: String) : ModuleOption()

    @RedisOption.Token("ARGS")
    class Arguments(vararg val args: String) : ModuleOption()
}
