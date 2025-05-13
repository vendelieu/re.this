package eu.vendeli.rethis.api.spec.common.response

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

@RedisOption.SkipName
data class GeoMember(
    val longitude: Double,
    val latitude: Double,
    val member: String,
)
