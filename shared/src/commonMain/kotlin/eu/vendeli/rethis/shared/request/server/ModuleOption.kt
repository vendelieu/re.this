package eu.vendeli.rethis.shared.request.server

import eu.vendeli.rethis.shared.annotations.RedisOption


sealed class ModuleOption {
    @RedisOption.Token("CONFIG")
    class Configs(val name: String, val value: String) : ModuleOption()

    @RedisOption.Token("ARGS")
    class Arguments(vararg val args: String) : ModuleOption()
}
