package eu.vendeli.rethis.shared.request.sortedset

sealed class ZPopCommonOption {
    data object MIN : ZPopCommonOption()
    data object MAX : ZPopCommonOption()
}
