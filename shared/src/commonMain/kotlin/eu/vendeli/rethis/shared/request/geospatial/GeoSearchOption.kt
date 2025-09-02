package eu.vendeli.rethis.shared.request.geospatial

import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.response.geospatial.GeoUnit

sealed class GeoSearchOption

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
