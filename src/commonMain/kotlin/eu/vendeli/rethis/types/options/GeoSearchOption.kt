package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.common.GeoUnit
import eu.vendeli.rethis.types.core.Argument
import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArg

sealed class GeoSearchOption

sealed class CenterPoint : GeoSearchOption()
class FROMMEMBER(
    val member: String,
) : CenterPoint(),
    VaryingArgument {
    override val data: List<Argument> = listOf("FROMMEMBER".toArg(), member.toArg())
}

class FROMLONLAT(
    longitude: Double,
    latitude: Double,
) : CenterPoint(),
    VaryingArgument {
    override val data = listOf("FROMLONLAT".toArg(), longitude.toArg(), latitude.toArg())
}

sealed class Shape : GeoSearchOption()
class BYRADIUS(
    radius: Double,
    unit: GeoUnit,
) : Shape(),
    VaryingArgument {
    override val data = listOf("BYRADIUS".toArg(), radius.toArg(), unit.toString().toArg())
}

class BYBOX(
    width: Double,
    height: Double,
    unit: GeoUnit,
) : Shape(),
    VaryingArgument {
    override val data = listOf("BYBOX".toArg(), width.toArg(), height.toArg(), unit.toString().toArg())
}
