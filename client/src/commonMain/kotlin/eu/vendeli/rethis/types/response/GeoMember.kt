package eu.vendeli.rethis.types.response

import eu.vendeli.rethis.types.common.VaryingArgument
import eu.vendeli.rethis.types.common.toArgument

data class GeoMember(
    val longitude: Double,
    val latitude: Double,
    val member: String,
) : VaryingArgument {
    override val data = listOf(longitude.toArgument(), latitude.toArgument(), member.toArgument())
}
