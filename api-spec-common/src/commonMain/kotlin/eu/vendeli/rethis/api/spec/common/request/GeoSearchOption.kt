package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer
import eu.vendeli.rethis.api.spec.common.response.GeoUnit

sealed class GeoSearchOption

@RedisOptionContainer
sealed class CenterPoint : GeoSearchOption()

@RedisOption
class FROMMEMBER(
    val member: String,
) : CenterPoint()

@RedisOption
class FROMLONLAT(
    longitude: Double,
    latitude: Double,
) : CenterPoint()

@RedisOptionContainer
sealed class Shape : GeoSearchOption()

@RedisOption
class BYRADIUS(
    radius: Double,
    unit: GeoUnit,
) : Shape()

@RedisOption
class BYBOX(
    width: Double,
    height: Double,
    unit: GeoUnit,
) : Shape()
