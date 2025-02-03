package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.response.GeoUnit
import eu.vendeli.rethis.types.common.Argument
import eu.vendeli.rethis.types.common.VaryingArgument
import eu.vendeli.rethis.types.common.toArgument

sealed class GeoSearchOption

sealed class CenterPoint : GeoSearchOption()
class FROMMEMBER(
    val member: String,
) : CenterPoint(),
    VaryingArgument {
    override val data: List<Argument> = listOf("FROMMEMBER".toArgument(), member.toArgument())
}

class FROMLONLAT(
    longitude: Double,
    latitude: Double,
) : CenterPoint(),
    VaryingArgument {
    override val data = listOf("FROMLONLAT".toArgument(), longitude.toArgument(), latitude.toArgument())
}

sealed class Shape : GeoSearchOption()
class BYRADIUS(
    radius: Double,
    unit: GeoUnit,
) : Shape(),
    VaryingArgument {
    override val data = listOf("BYRADIUS".toArgument(), radius.toArgument(), unit.toString().toArgument())
}

class BYBOX(
    width: Double,
    height: Double,
    unit: GeoUnit,
) : Shape(),
    VaryingArgument {
    override val data =
        listOf("BYBOX".toArgument(), width.toArgument(), height.toArgument(), unit.toString().toArgument())
}
