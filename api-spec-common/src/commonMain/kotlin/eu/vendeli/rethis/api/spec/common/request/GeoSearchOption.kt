package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer
import eu.vendeli.rethis.api.spec.common.response.GeoUnit

sealed class GeoSearchOption

@RedisOptionContainer
sealed class CenterPoint : GeoSearchOption()

@RedisOption.Token("FROMMEMBER")
class FromMember(
    val member: String,
) : CenterPoint()

@RedisOption.Token("FROMLONLAT")
class FromLongitudeLatitude(
    val longitude: Double,
    val latitude: Double,
) : CenterPoint()

@RedisOptionContainer
sealed class Shape : GeoSearchOption()

@RedisOption.Token("BYRADIUS")
class ByRadius(
    val radius: Double,
    val unit: GeoUnit,
) : Shape()

@RedisOption.Token("BYBOX")
class ByBox(
    val width: Double,
    val height: Double,
    val unit: GeoUnit,
) : Shape()
