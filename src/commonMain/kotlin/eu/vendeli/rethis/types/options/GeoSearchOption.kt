package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.common.GeoUnit
import eu.vendeli.rethis.types.core.PairArgument
import eu.vendeli.rethis.types.core.TripleArgument

sealed class GeoSearchOption

sealed class CenterPoint : GeoSearchOption()
data class FROMMEMBER(
    val member: String,
) : CenterPoint(),
    PairArgument<String, String> {
    override val arg = "FROMMEMBER" to member
}

data class FROMLONLAT(
    val longitude: Double,
    val latitude: Double,
) : CenterPoint(),
    TripleArgument<String, Double, Double> {
    override val arg = Triple("FROMLONLAT", longitude, latitude)
}

sealed class Shape : GeoSearchOption()
data class BYRADIUS(
    val radius: Double,
    val unit: GeoUnit,
) : Shape(),
    TripleArgument<String, Double, String> {
    override val arg = Triple("BYRADIUS", radius, unit.toString())
}

data class BYBOX(
    val width: Double,
    val height: Double,
    val unit: GeoUnit,
) : Shape(),
    TripleArgument<Double, Double, String> {
    override val arg = Triple(width, height, unit.toString())
}
