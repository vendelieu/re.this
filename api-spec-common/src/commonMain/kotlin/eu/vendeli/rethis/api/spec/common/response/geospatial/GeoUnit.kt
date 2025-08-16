package eu.vendeli.rethis.api.spec.common.response.geospatial

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

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
