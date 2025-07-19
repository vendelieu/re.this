package eu.vendeli.rethis.api.spec.common.response.geospatial

data class GeoSearchResult(
    val member: String,
    val distance: Double?,
    val coordinates: GeoPosition?,
    val hash: Long?,
)
