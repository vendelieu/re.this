package eu.vendeli.rethis.api.spec.common.request

sealed class ZPopCommonOption {
    data object MIN : ZPopCommonOption()
    data object MAX : ZPopCommonOption()
}
