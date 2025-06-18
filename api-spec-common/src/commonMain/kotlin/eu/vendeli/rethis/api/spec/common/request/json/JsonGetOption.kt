package eu.vendeli.rethis.api.spec.common.request.json

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption


sealed class JsonGetOption {
    @RedisOption.Token("INDENT")
    class Indent(val indent: String) : JsonGetOption()

    @RedisOption.Token("NEWLINE")
    class Newline(val newline: String) : JsonGetOption()

    @RedisOption.Token("SPACE")
    class Space(val space: String) : JsonGetOption()

    class Paths(vararg val path: String) : JsonGetOption()
}
