package eu.vendeli.rethis.api.spec.common.request.sortedset

sealed class ZPopCommonOption {
    data object MIN : ZPopCommonOption()
    data object MAX : ZPopCommonOption()
}
