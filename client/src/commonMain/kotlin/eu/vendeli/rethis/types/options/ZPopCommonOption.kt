package eu.vendeli.rethis.types.options

sealed class ZPopCommonOption {
    data object MIN : ZPopCommonOption()
    data object MAX : ZPopCommonOption()
}
