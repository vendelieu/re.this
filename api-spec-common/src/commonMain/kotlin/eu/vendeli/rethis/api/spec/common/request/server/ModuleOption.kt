package eu.vendeli.rethis.api.spec.common.request.server

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption


sealed class ModuleOption {
    @RedisOption.Token("CONFIG")
    class Configs(val name: String, val value: String) : ModuleOption()

    @RedisOption.Token("ARGS")
    class Arguments(vararg val args: String) : ModuleOption()
}
