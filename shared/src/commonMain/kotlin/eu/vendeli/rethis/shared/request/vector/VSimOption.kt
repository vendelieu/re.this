package eu.vendeli.rethis.shared.request.vector

import eu.vendeli.rethis.shared.annotations.RedisOption

sealed class VSimOption {
    @RedisOption.Token("WITHSCORES")
    data object WithScores : VSimOption()

    @RedisOption.Token("COUNT")
    class Count(@RedisOption.Name("count") val n: Long) : VSimOption()

    @RedisOption.Token("EF")
    class Ef(@RedisOption.Name("search-exploration-factor") val maxSearchExpansion: Long) : VSimOption()

    @RedisOption.Token("FILTER")
    class Filter(val expression: String) : VSimOption()

    @RedisOption.Token("FILTER-EF")
    class FilterEf(val maxFilteringEffort: Long) : VSimOption()

    @RedisOption.Token("TRUTH")
    data object Truth : VSimOption()

    @RedisOption.Token("NOTHREAD")
    data object NoThread : VSimOption()

    @RedisOption.Token("EPSILON")
    class Epsilon(@RedisOption.Name("max_distance") val delta: Double) : VSimOption()
}
