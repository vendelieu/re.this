package eu.vendeli.rethis.types.common

data class GeoSearchResult(
    val member: String,
    val distance: Double?,
    val coordinates: GeoPosition?,
    val hash: Long?,
)
