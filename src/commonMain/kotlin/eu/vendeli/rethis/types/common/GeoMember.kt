package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArg

data class GeoMember(
    val longitude: Double,
    val latitude: Double,
    val member: String,
) : VaryingArgument {
    override val data = listOf(longitude.toArg(), latitude.toArg(), member.toArg())
}
