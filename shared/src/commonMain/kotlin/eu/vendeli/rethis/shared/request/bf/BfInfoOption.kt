package eu.vendeli.rethis.shared.request.bf

import eu.vendeli.rethis.shared.annotations.RedisOption

sealed class BfInfoOption {
    @RedisOption.Token("CAPACITY")
    data object Capacity : BfInfoOption()

    @RedisOption.Token("SIZE")
    data object Size : BfInfoOption()

    @RedisOption.Token("FILTERS")
    data object Filters : BfInfoOption()

    @RedisOption.Token("ITEMS")
    data object Items : BfInfoOption()

    @RedisOption.Token("EXPANSION")
    data object Expansion : BfInfoOption()
}
