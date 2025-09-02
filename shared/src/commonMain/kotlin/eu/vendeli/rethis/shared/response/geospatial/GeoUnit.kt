package eu.vendeli.rethis.shared.response.geospatial

import eu.vendeli.rethis.shared.annotations.RedisOption

enum class GeoUnit(
    private val literal: String,
) {
    @RedisOption.Name("M")
    METERS("M"),

    @RedisOption.Name("KM")
    KILOMETERS("KM"),

    @RedisOption.Name("MI")
    MILES("MI"),

    @RedisOption.Name("FT")
    FEET("FT"),
    ;

    override fun toString(): String = literal
}
