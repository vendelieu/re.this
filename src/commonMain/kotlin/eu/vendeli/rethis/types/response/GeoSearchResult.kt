package eu.vendeli.rethis.types.response

data class GeoSearchResult(
    val member: String,
    val distance: Double?,
    val coordinates: GeoPosition?,
    val hash: Long?,
)
