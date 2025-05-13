package eu.vendeli.rethis.types.options

sealed class LInsertPlace {
    data object BEFORE : LInsertPlace()
    data object AFTER : LInsertPlace()
}
