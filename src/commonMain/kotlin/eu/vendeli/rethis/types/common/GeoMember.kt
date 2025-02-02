package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArgument

data class GeoMember(
    val longitude: Double,
    val latitude: Double,
    val member: String,
) : VaryingArgument {
    override val data = listOf(longitude.toArgument(), latitude.toArgument(), member.toArgument())
}
