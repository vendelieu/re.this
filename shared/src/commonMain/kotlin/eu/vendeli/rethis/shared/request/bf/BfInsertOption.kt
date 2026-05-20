package eu.vendeli.rethis.shared.request.bf

import eu.vendeli.rethis.shared.annotations.RedisOption

sealed class BfInsertOption {
    @RedisOption.Token("CAPACITY")
    class Capacity(val capacity: Long) : BfInsertOption()

    @RedisOption.Token("ERROR")
    class Error(val error: Double) : BfInsertOption()

    @RedisOption.Token("EXPANSION")
    class Expansion(val expansion: Long) : BfInsertOption()

    @RedisOption.Token("NOCREATE")
    data object NoCreate : BfInsertOption()

    @RedisOption.Token("NONSCALING")
    data object NonScaling : BfInsertOption()
}
