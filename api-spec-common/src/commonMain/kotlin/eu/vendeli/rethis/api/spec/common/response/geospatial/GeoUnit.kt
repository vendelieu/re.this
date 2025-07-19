package eu.vendeli.rethis.api.spec.common.response.geospatial

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

enum class GeoUnit(
    private val literal: String,
) {
    @RedisOption.Name("M")
    METERS("m"),
    @RedisOption.Name("KM")
    KILOMETERS("km"),
    @RedisOption.Name("MI")
    MILES("mi"),
    @RedisOption.Name("FT")
    FEET("ft"),
    ;

    override fun toString(): String = literal
}
