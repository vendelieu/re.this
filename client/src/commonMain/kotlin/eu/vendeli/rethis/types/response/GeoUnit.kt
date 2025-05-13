package eu.vendeli.rethis.types.response

enum class GeoUnit(
    private val literal: String,
) {
    METERS("m"),
    KILOMETERS("km"),
    MILES("mi"),
    FEET("ft"),
    ;

    override fun toString(): String = literal
}
