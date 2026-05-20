package eu.vendeli.rethis.shared.request.search

import eu.vendeli.rethis.shared.annotations.RedisOption

sealed class FtProfileQueryType {
    @RedisOption.Token("SEARCH")
    data object Search : FtProfileQueryType()

    @RedisOption.Token("AGGREGATE")
    data object Aggregate : FtProfileQueryType()
}
