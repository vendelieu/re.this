package eu.vendeli.rethis.types.options

sealed class GeoAddOption {
    sealed class UpsertMode : GeoAddOption()
    data object NX : UpsertMode()
    data object XX : UpsertMode()
}
