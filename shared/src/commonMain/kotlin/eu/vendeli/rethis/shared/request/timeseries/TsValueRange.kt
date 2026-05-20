package eu.vendeli.rethis.shared.request.timeseries

import eu.vendeli.rethis.shared.annotations.RedisOption

@RedisOption.Token("FILTER_BY_VALUE")
data class TsValueRange(
    val min: Double,
    val max: Double,
)
