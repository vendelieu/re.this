package eu.vendeli.rethis.shared.response.geospatial

data class GeoSearchResult(
    val member: String,
    val distance: Double?,
    val coordinates: GeoPosition?,
    val hash: Long?,
)
