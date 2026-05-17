package eu.vendeli.rethis.shared.request.vector

import eu.vendeli.rethis.shared.annotations.RedisOption

sealed class VSimOption {
    @RedisOption.Token("WITHSCORES")
    data object WithScores : VSimOption()

    @RedisOption.Token("COUNT")
    class Count(val n: Long) : VSimOption()

    @RedisOption.Token("EF")
    class Ef(val maxSearchExpansion: Long) : VSimOption()

    @RedisOption.Token("FILTER")
    class Filter(val expression: String) : VSimOption()

    @RedisOption.Token("FILTER-EF")
    class FilterEf(val maxFilteringEffort: Long) : VSimOption()

    @RedisOption.Token("TRUTHY")
    data object Truthy : VSimOption()

    @RedisOption.Token("NOTHREAD")
    data object NoThread : VSimOption()

    @RedisOption.Token("EPSILON")
    class Epsilon(val delta: Double) : VSimOption()
}
