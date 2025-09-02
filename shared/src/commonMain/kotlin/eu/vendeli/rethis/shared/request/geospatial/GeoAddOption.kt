package eu.vendeli.rethis.shared.request.geospatial

sealed class GeoAddOption {
    sealed class UpsertMode : GeoAddOption()
    data object NX : UpsertMode()
    data object XX : UpsertMode()
}
