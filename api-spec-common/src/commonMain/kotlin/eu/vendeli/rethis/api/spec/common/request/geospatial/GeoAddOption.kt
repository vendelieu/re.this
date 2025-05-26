package eu.vendeli.rethis.api.spec.common.request.geospatial

sealed class GeoAddOption {
    sealed class UpsertMode : GeoAddOption()
    data object NX : UpsertMode()
    data object XX : UpsertMode()
}
