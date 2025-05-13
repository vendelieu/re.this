package eu.vendeli.rethis.types.options

sealed class FunctionRestoreOption {
    data object FLUSH : FunctionRestoreOption()
    data object APPEND : FunctionRestoreOption()
    data object REPLACE : FunctionRestoreOption()
}
