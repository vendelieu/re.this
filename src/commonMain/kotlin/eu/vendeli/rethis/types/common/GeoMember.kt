package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.types.core.TripleArgument

data class GeoMember(
    val longitude: Double,
    val latitude: Double,
    val member: String,
) : TripleArgument<Double, Double, String> {
    override val arg = Triple(longitude, latitude, member)
}
