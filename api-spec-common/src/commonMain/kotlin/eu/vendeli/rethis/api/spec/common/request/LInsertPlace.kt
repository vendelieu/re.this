package eu.vendeli.rethis.api.spec.common.request

sealed class LInsertPlace {
    data object BEFORE : LInsertPlace()
    data object AFTER : LInsertPlace()
}
